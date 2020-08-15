package me.zeroeightsix.kami.feature.module.movement;

import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.EntityEvent;
import me.zeroeightsix.kami.event.KamiEvent;
import me.zeroeightsix.kami.event.MoveEntityFluidEvent;
import me.zeroeightsix.kami.event.PacketEvent;
import me.zeroeightsix.kami.feature.module.Module;
import me.zeroeightsix.kami.mixin.client.IEntityVelocityUpdateS2CPacket;
import me.zeroeightsix.kami.mixin.client.IExplosionS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;

/**
 * Created by 086 on 16/11/2017.
 */
@Module.Info(name = "Velocity", description = "Modify knockback impact", category = Module.Category.MOVEMENT)
public class Velocity extends Module {

    @Setting(name = "Horizontal")
    private float horizontal = 0f;
    @Setting(name = "Vertical")
    private float vertical = 0f;

    @EventHandler
    private Listener<PacketEvent.Receive> packetEventListener = new Listener<>(event -> {
        if (mc.player == null) return;
        if (event.getEra() == KamiEvent.Era.PRE) {
            if (event.getPacket() instanceof EntityVelocityUpdateS2CPacket) {
                EntityVelocityUpdateS2CPacket velocity = (EntityVelocityUpdateS2CPacket) event.getPacket();
                if (velocity.getId() == mc.player.getEntityId()) {
                    if (horizontal == 0 && vertical == 0) event.cancel();
                    IEntityVelocityUpdateS2CPacket xyz = (IEntityVelocityUpdateS2CPacket) velocity;
                    xyz.setVelocityX((int) (xyz.getVelocityX() * horizontal));
                    xyz.setVelocityY((int) (xyz.getVelocityY() * vertical));
                    xyz.setVelocityZ((int) (xyz.getVelocityZ() * horizontal));
                }
            } else if (event.getPacket() instanceof ExplosionS2CPacket) {
                if (horizontal == 0 && vertical == 0) event.cancel();
                IExplosionS2CPacket xyz = (IExplosionS2CPacket) event.getPacket();
                xyz.setPlayerVelocityX(xyz.getPlayerVelocityX() * horizontal);
                xyz.setPlayerVelocityY(xyz.getPlayerVelocityY() * vertical);
                xyz.setPlayerVelocityZ(xyz.getPlayerVelocityZ() * horizontal);
            }
        }
    });

    @EventHandler
    private Listener<EntityEvent.EntityCollision> entityCollisionListener = new Listener<>(event -> {
        if (event.getEntity() == mc.player) {
            if (horizontal == 0 && vertical == 0) {
                event.cancel();
                return;
            }
            event.setX(-event.getX() * horizontal);
            event.setY(0);
            event.setZ(-event.getZ() * horizontal);
        }
    });

    @EventHandler
    private Listener<MoveEntityFluidEvent> moveEntityFluidEventListener = new Listener<>(event -> {
        if (event.getEntity() == mc.player) {
            event.setMovement(event.getMovement().multiply(horizontal, vertical, horizontal));
        }
    });

}
