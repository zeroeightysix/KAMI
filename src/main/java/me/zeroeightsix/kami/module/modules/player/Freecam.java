package me.zeroeightsix.kami.module.modules.player;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.events.PacketEvent;
import me.zeroeightsix.kami.event.events.PlayerMoveEvent;
import me.zeroeightsix.kami.event.events.TickEvent;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.packet.PlayerInputC2SPacket;
import net.minecraft.server.network.packet.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;

/**
 * Created by 086 on 22/12/2017.
 */
@Module.Info(name = "Freecam", category = Module.Category.PLAYER, description = "Leave your body and trascend into the realm of the gods")
public class Freecam extends Module {

    private Setting<Integer> speed = register(Settings.i("Speed", 5)); // /100 in practice

    private double x, y, z;
    private float pitch, yaw;

    private OtherClientPlayerEntity clonedPlayer;

    private boolean isRidingEntity;
    private Entity ridingEntity;

    @Override
    public void onEnable() {
        if (mc.player != null) {
            isRidingEntity = mc.player.getVehicle() != null;

            if (mc.player.getVehicle() == null) {
                x = mc.player.x;
                y = mc.player.y;
                z = mc.player.z;
            } else {
                ridingEntity = mc.player.getVehicle();
                mc.player.stopRiding();
            }

            pitch = mc.player.pitch;
            yaw = mc.player.yaw;

            clonedPlayer = new OtherClientPlayerEntity(mc.world, mc.getSession().getProfile());
            clonedPlayer.copyFrom(mc.player);
            clonedPlayer.headYaw = mc.player.headYaw;
            mc.world.addEntity(-100, clonedPlayer);
            mc.player.abilities.flying = true;
            mc.player.abilities.setFlySpeed(speed.getValue() / 100f);
            mc.player.noClip = true;
        }
    }

    @Override
    public void onDisable() {
        PlayerEntity localPlayer = mc.player;
        if (localPlayer != null) {
            mc.player.setPositionAndAngles(x, y, z, yaw, pitch);
            mc.world.removeEntity(-100);
            clonedPlayer = null;
            x = y = z = 0.D;
            pitch = yaw = 0.f;
            mc.player.abilities.flying = false; //getModManager().getMod("ElytraFlight").isEnabled();
            mc.player.abilities.setFlySpeed(0.05f);
            mc.player.noClip = false;
            mc.player.setVelocity(Vec3d.ZERO);

            if (isRidingEntity) {
                mc.player.startRiding(ridingEntity, true);
            }
        }
    }

    @EventHandler
    private Listener<TickEvent.Client> updateListener = new Listener<>(event -> {
        mc.player.abilities.flying = true;
        mc.player.abilities.setFlySpeed(speed.getValue() / 100f);
        mc.player.noClip = true;
        mc.player.onGround = false;
        mc.player.fallDistance = 0;
    });

    @EventHandler
    private Listener<PlayerMoveEvent> moveListener = new Listener<>(event -> {
        mc.player.noClip = true;
    });

    @EventHandler
    private Listener<PacketEvent.Send> sendListener = new Listener<>(event -> {
        if (event.getPacket() instanceof PlayerMoveC2SPacket || event.getPacket() instanceof PlayerInputC2SPacket) {
            event.cancel();
        }
    });

}
