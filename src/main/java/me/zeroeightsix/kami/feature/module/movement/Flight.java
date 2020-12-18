package me.zeroeightsix.kami.feature.module.movement;

import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.TickEvent;
import me.zeroeightsix.kami.feature.hidden.PlayerMovementSpoofer;
import me.zeroeightsix.kami.feature.module.Module;
import me.zeroeightsix.kami.util.EntityUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;

@Module.Info(category = Module.Category.MOVEMENT, description = "Makes the player fly", name = "Flight")
public class Flight extends Module {

    @Setting(name = "Speed")
    private @Setting.Constrain.Range(min = 0, max = 10, step = 0.5) float speed = 1f;
    @Setting(name = "Mode")
    private FlightMode mode = FlightMode.VANILLA;

    private transient FlightCapabilities capabilities;

    @EventHandler
    private Listener<TickEvent.InGame> updateListener = new Listener<>(event -> {
        ClientPlayerEntity player = event.getPlayer();
        switch (mode) {
            case STATIC:
                player.abilities.flying = false;
                player.setVelocity(Vec3d.ZERO);
                player.flyingSpeed = speed;

                if (mc.options.keyJump.isPressed()) {
                    player.addVelocity(0, speed, 0);
                }
                if (mc.options.keySneak.isPressed()) {
                    player.addVelocity(0, -speed, 0);
                }
                break;
            case VANILLA:
                player.abilities.setFlySpeed(speed / 20f);
                player.abilities.flying = true;
                if (player.abilities.creativeMode) return;
                player.abilities.allowFlying = true;
                break;
            case PACKET:
                if (player.isDead()) return;

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
                    float yaw = player.yaw + angle;
                    player.setVelocity(EntityUtil.getRelativeX(yaw) * 0.2f, player.getVelocity().y, EntityUtil.getRelativeZ(yaw) * 0.2f);
                }

                EntityUtil.updateVelocityY(player, 0);

                PlayerMovementSpoofer.INSTANCE.setPosition(new Vec3d(player.getX() + player.getVelocity().x, player.getY() + (MinecraftClient.getInstance().options.keyJump.isPressed() ? 0.0622 : 0) - (MinecraftClient.getInstance().options.keySneak.isPressed() ? 0.0622 : 0), player.getZ() + player.getVelocity().z),
                        PlayerMovementSpoofer.Priority.NORMAL,
                        PlayerMovementSpoofer.Mode.INSTANT);
                PlayerMovementSpoofer.INSTANCE.setPosition(new Vec3d(player.getX() + player.getVelocity().x, player.getY() - 42069 /* nice */, player.getZ() + player.getVelocity().z),
                        PlayerMovementSpoofer.Priority.NORMAL,
                        PlayerMovementSpoofer.Mode.INSTANT);
                break;
        }
    });

    @Override
    public void onEnable() {
        this.capabilities = mc.player != null ?
                new FlightCapabilities(mc.player.abilities.flying, mc.player.abilities.getFlySpeed(), mc.player.abilities.allowFlying) :
                FlightCapabilities.SURVIVAL;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (mc.player != null)
            capabilities.apply();
    }

    public enum FlightMode {
        VANILLA, STATIC, PACKET
    }

    private static class FlightCapabilities {
        final boolean flying;
        final float flightSpeed;
        final boolean allowFlying;

        static FlightCapabilities SURVIVAL = new FlightCapabilities(false, 0.05f, false);

        private FlightCapabilities(boolean flying, float flightSpeed, boolean allowFlying) {
            this.flying = flying;
            this.flightSpeed = flightSpeed;
            this.allowFlying = allowFlying;
        }

        public void apply() {
            assert mc.player != null;

            mc.player.abilities.flying = this.flying;
            mc.player.abilities.setFlySpeed(this.flightSpeed);
            mc.player.abilities.allowFlying = this.allowFlying;
        }
    }

}
