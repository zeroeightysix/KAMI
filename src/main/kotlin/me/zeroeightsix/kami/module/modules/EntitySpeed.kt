package me.zeroeightsix.kami.module.modules.movement;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.events.CanBeSteeredEvent;
import me.zeroeightsix.kami.event.events.RenderHudEvent;
import me.zeroeightsix.kami.event.events.TickEvent;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.util.EntityUtil;
import net.minecraft.client.input.Input;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.HorseBaseEntity;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.world.chunk.EmptyChunk;

/**
 * Created by 086 on 16/12/2017.
 */
@Module.Info(name = "EntitySpeed", category = Module.Category.MOVEMENT, description = "Abuse client-sided movement to shape sound barrier breaking rideables")
public class EntitySpeed extends Module {

    private Setting<Double> speed = register(Settings.d("Speed", 1d));
    private Setting<Boolean> antiStuck = register(Settings.b("AntiStuck"));
    private Setting<Boolean> flight = register(Settings.b("Flight", false));
    private Setting<Boolean> wobble = register(Settings.booleanBuilder("Wobble").withValue(true).withVisibility(b -> flight.getValue()).build());
    private static Setting<Float> opacity = Settings.f("Boat opacity", .5f);

    public EntitySpeed() {
        register(opacity);
    }

    @EventHandler
    private Listener<TickEvent.Client> updateListener = new Listener<>(event -> {
        if ((mc.world != null) && (mc.player.getVehicle() != null)) {
            Entity riding = mc.player.getVehicle();
            if (riding instanceof PigEntity || riding instanceof HorseBaseEntity) {
                steerEntity(riding);
            } else if (riding instanceof BoatEntity) {
                steerBoat(getBoat());
            }
        }
    });

    private void steerEntity(Entity entity) {
        if (!flight.getValue()) {
            EntityUtil.updateVelocityY(entity, -0.4D);
        }

        if (flight.getValue()) {
            if (mc.options.keyJump.isPressed()) {
                EntityUtil.updateVelocityY(entity, speed.getValue());
            }
            else if (mc.options.keyForward.isPressed() || mc.options.keyBack.isPressed())
                EntityUtil.updateVelocityY(entity, wobble.getValue() ? Math.sin(mc.player.age) : 0);
        }

        moveForward(entity, speed.getValue() * 3.8D);

        if (entity instanceof HorseEntity){
            entity.yaw = mc.player.yaw;
        }
    }

    private void steerBoat(BoatEntity boat) {
        if (boat == null) return;

        int angle;

        boolean forward = mc.options.keyForward.isPressed();
        boolean left = mc.options.keyLeft.isPressed();
        boolean right = mc.options.keyRight.isPressed();
        boolean back = mc.options.keyBack.isPressed();
        if (!(forward && back)) {
            EntityUtil.updateVelocityY(boat, 0);
        }
        if (mc.options.keyJump.isPressed()) {
            boat.setVelocity(boat.getVelocity().add(0, speed.getValue() / 2d, 0));
        }

        if (!forward && !left && !right && !back) return;
        if (left && right) angle = forward ? 0 : back ? 180 : -1;
        else if (forward && back) angle = left ? -90 : (right ? 90 : -1);
        else {
            angle = left ? -90 : (right ? 90 : 0);
            if (forward) angle /= 2;
            else if (back) angle = 180 - (angle / 2);
        }

        if (angle == -1) return;
        float yaw = mc.player.yaw + angle;
        boat.setVelocity(EntityUtil.getRelativeX(yaw) * speed.getValue(), boat.getVelocity().y, EntityUtil.getRelativeZ(yaw) * speed.getValue());
    }

    @EventHandler
    public Listener<RenderHudEvent> renderListener = new Listener<>(event -> {
        BoatEntity boat = getBoat();
        if (boat == null) return;
        boat.yaw = mc.player.yaw;
        boat.setInputs(false, false, false, false); // Make sure the boat doesn't turn etc (params: isLeftDown, isRightDown, isForwardDown, isBackDown)
    });

    private BoatEntity getBoat() {
        if (mc.player.getVehicle() != null && mc.player.getVehicle() instanceof BoatEntity)
            return (BoatEntity) mc.player.getVehicle();
        return null;
    }

    private void moveForward(Entity entity, double speed) {
        if (entity != null) {
            Input movementInput = mc.player.input;

            double forward = movementInput.movementForward;
            double strafe = movementInput.movementSideways;
            boolean movingForward = forward != 0;
            boolean movingStrafe = strafe != 0;
            float yaw = mc.player.yaw;

            if (!movingForward && !movingStrafe) {
                setEntitySpeed(entity, 0, 0);
            } else {
                if (forward != 0.0D) {
                    if (strafe > 0.0D) {
                        yaw += (forward > 0.0D ? -45 : 45);
                    } else if (strafe < 0.0D) {
                        yaw += (forward > 0.0D ? 45 : -45);
                    }
                    strafe = 0.0D;
                    if (forward > 0.0D) {
                        forward = 1.0D;
                    } else {
                        forward = -1.0D;
                    }
                }

                double motX = (forward * speed * Math.cos(Math.toRadians(yaw + 90.0F)) + strafe * speed * Math.sin(Math.toRadians(yaw + 90.0F)));
                double motZ = (forward * speed * Math.sin(Math.toRadians(yaw + 90.0F)) - strafe * speed * Math.cos(Math.toRadians(yaw + 90.0F)));

                if (isBorderingChunk(entity, motX, motZ))
                    motX = motZ = 0;

                setEntitySpeed(entity, motX, motZ);
            }
        }
    }

    private void setEntitySpeed(Entity entity, double motX, double motZ) {
        entity.setVelocity(motX, entity.getVelocity().y, motZ);
    }

    private boolean isBorderingChunk(Entity entity, double motX, double motZ) {
        return antiStuck.getValue() && mc.world.getChunk((int) (entity.x + motX) >> 4, (int) (entity.z + motZ) >> 4) instanceof EmptyChunk;
    }

    public static float getOpacity() {
        return opacity.getValue();
    }

    public Listener<CanBeSteeredEvent> eventListener = new Listener<>(event -> {
        event.setCanBeSteered(event.canBeSteered() || isEnabled());
    });

}
