package me.zeroeightsix.kami.module.modules.movement;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.KamiEvent;
import me.zeroeightsix.kami.event.events.EntityEvent;
import me.zeroeightsix.kami.event.events.MoveEntityFluidEvent;
import me.zeroeightsix.kami.event.events.PacketEvent;
import me.zeroeightsix.kami.mixin.client.IEntityVelocityUpdateS2CPacket;
import me.zeroeightsix.kami.mixin.client.IPlayerMoveC2SPacket;
import me.zeroeightsix.kami.module.ModulePlay;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraft.client.network.packet.EntityVelocityUpdateS2CPacket;
import net.minecraft.client.network.packet.ExplosionS2CPacket;

/**
 * Created by 086 on 16/11/2017.
 */
@ModulePlay.Info(name = "Velocity", description = "Modify knockback impact", category = ModulePlay.Category.MOVEMENT)
public class Velocity extends ModulePlay {

    private Setting<Float> horizontal = register(Settings.f("Horizontal", 0));
    private Setting<Float> vertical = register(Settings.f("Vertical", 0));

    @EventHandler
    private Listener<PacketEvent.Receive> packetEventListener = new Listener<>(event -> {
        if (event.getEra() == KamiEvent.Era.PRE) {
            if (event.getPacket() instanceof EntityVelocityUpdateS2CPacket) {
                EntityVelocityUpdateS2CPacket velocity = (EntityVelocityUpdateS2CPacket) event.getPacket();
                if (velocity.getId() == mc.player.getEntityId()) {
                    if (horizontal.getValue() == 0 && vertical.getValue() == 0) event.cancel();
                    IEntityVelocityUpdateS2CPacket xyz = (IEntityVelocityUpdateS2CPacket) velocity;
                    xyz.setVelocityX((int) (xyz.getVelocityX() * horizontal.getValue()));;
                    xyz.setVelocityY((int) (xyz.getVelocityY() * vertical.getValue()));
                    xyz.setVelocityZ((int) (xyz.getVelocityZ() * horizontal.getValue()));;
                }
            } else if (event.getPacket() instanceof ExplosionS2CPacket) {
                if (horizontal.getValue() == 0 && vertical.getValue() == 0) event.cancel();
                IPlayerMoveC2SPacket xyz = (IPlayerMoveC2SPacket) event.getPacket();
                xyz.setX(xyz.getX() );
                xyz.setX((int) (xyz.getX() * horizontal.getValue()));;
                xyz.setY((int) (xyz.getY() * vertical.getValue()));
                xyz.setZ((int) (xyz.getZ() * horizontal.getValue()));;
            }
        }
    });

    @EventHandler
    private Listener<EntityEvent.EntityCollision> entityCollisionListener = new Listener<>(event -> {
        if (event.getEntity() == mc.player) {
            if (horizontal.getValue() == 0 && vertical.getValue() == 0) {
                event.cancel();
                return;
            }
            event.setX(-event.getX() * horizontal.getValue());
            event.setY(0);
            event.setZ(-event.getZ() * horizontal.getValue());
        }
    });

    @EventHandler
    private Listener<MoveEntityFluidEvent> moveEntityFluidEventListener = new Listener<>(event -> {
        if (event.getEntity() == mc.player) {
            event.setMovement(event.getMovement().multiply(horizontal.getValue(), vertical.getValue(), horizontal.getValue()));
        }
    });

}
