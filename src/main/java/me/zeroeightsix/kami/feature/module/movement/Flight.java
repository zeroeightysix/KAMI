package me.zeroeightsix.kami.feature.module.movement;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting;
import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Settings;
import me.zeroeightsix.kami.event.events.TickEvent;
import me.zeroeightsix.kami.feature.module.Module;
import me.zeroeightsix.kami.util.EntityUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;

/**
 * Created by 086 on 25/08/2017.
 */
@Module.Info(category = Module.Category.MOVEMENT, description = "Makes the player fly", name = "Flight")
public class Flight extends Module {

    @Setting(name = "Speed")
    private @Setting.Constrain.Range(min = 0, max = 10, step = 0.5) float speed = 1f;
    @Setting(name = "Mode")
    private FlightMode mode = FlightMode.VANILLA;

    @EventHandler
    private Listener<TickEvent.Client.InGame> updateListener = new Listener<>(event -> {
        switch (mode) {
            case STATIC:
                mc.player.abilities.flying = false;
                mc.player.setVelocity(Vec3d.ZERO);
                mc.player.flyingSpeed = speed;

                if (mc.options.keyJump.isPressed()) {
                    mc.player.addVelocity(0, speed, 0);
                }
                if (mc.options.keySneak.isPressed()) {
                    mc.player.addVelocity(0, -speed, 0);
                }
                break;
            case VANILLA:
                mc.player.abilities.setFlySpeed(speed / 20f);
                mc.player.abilities.flying = true;
                if (mc.player.abilities.creativeMode) return;
                mc.player.abilities.allowFlying = true;
                break;
            case PACKET:
                int angle;

                boolean forward = mc.options.keyForward.isPressed();
                boolean left = mc.options.keyLeft.isPressed();
                boolean right = mc.options.keyRight.isPressed();
                boolean back = mc.options.keyBack.isPressed();

                if (left && right) angle = forward ? 0 : back ? 180 : -1;
                else if (forward && back) angle = left ? -90 : (right ? 90 : -1);
                else {
                    angle = left ? -90 : (right ? 90 : 0);
                    if (forward) angle /= 2;
                    else if (back) angle = 180 - (angle / 2);
                }

                if (angle != -1 && (forward || left || right || back)) {
                    float yaw = mc.player.yaw + angle;
                    mc.player.setVelocity(EntityUtil.getRelativeX(yaw) * 0.2f, mc.player.getVelocity().y, EntityUtil.getRelativeZ(yaw) * 0.2f);
                }

                EntityUtil.updateVelocityY(mc.player, 0);
                mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.Both(mc.player.getX() + mc.player.getVelocity().x, mc.player.getY() + (MinecraftClient.getInstance().options.keyJump.isPressed() ? 0.0622 : 0) - (MinecraftClient.getInstance().options.keySneak.isPressed() ? 0.0622 : 0), mc.player.getZ() + mc.player.getVelocity().z, mc.player.yaw, mc.player.pitch, false));
                mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.Both(mc.player.getX() + mc.player.getVelocity().x, mc.player.getY() - 42069 /* nice */, mc.player.getZ() + mc.player.getVelocity().z, mc.player.yaw, mc.player.pitch, true));
                break;
        }
    });

    @Override
    public void onDisable() {
        super.onDisable();

        if (mode == FlightMode.VANILLA && mc.player != null) {
            mc.player.abilities.flying = false;
            mc.player.abilities.setFlySpeed(0.05f);
            if (mc.player.abilities.creativeMode) return;
            mc.player.abilities.allowFlying = false;
        }
    }

    public enum FlightMode {
        VANILLA, STATIC, PACKET
    }

}
