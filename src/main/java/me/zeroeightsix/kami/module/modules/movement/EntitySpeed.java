package me.zeroeightsix.kami.module.modules.movement;

import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraft.entity.passive.AbstractHorse;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.util.MovementInput;
import net.minecraft.world.chunk.EmptyChunk;

/**
 * Created by 086 on 16/12/2017.
 */
@Module.Info(name = "EntitySpeed", category = Module.Category.MOVEMENT, description = "Abuse client-sided movement to shape sound barrier breaking rideables")
public class EntitySpeed extends Module {

    private Setting<Float> speed = register(Settings.f("Speed", 1));
    private Setting<Boolean> antiStuck = register(Settings.b("AntiStuck"));

    @Override
    public void onUpdate() {
        if (isEnabled() && (mc.world != null) && (mc.player.getRidingEntity() != null) && (mc.player.getRidingEntity() instanceof EntityPig || mc.player.getRidingEntity() instanceof AbstractHorse))
        {
            if (mc.player.getRidingEntity().onGround) {
                mc.player.getRidingEntity().motionY = 0.4D;
            }
            mc.player.getRidingEntity().motionY = -0.4D;

            setMoveSpeedEntity(speed.getValue() * 3.8D);

            if (mc.player.getRidingEntity() instanceof EntityHorse){
                mc.player.getRidingEntity().rotationYaw = mc.player.rotationYaw;
            }
        }
    }

    private void setMoveSpeedEntity(double speed) {
        if (mc.player.getRidingEntity() != null)
        {
            MovementInput movementInput = mc.player.movementInput;

            double forward = movementInput.moveForward;
            double strafe = movementInput.moveStrafe;
            float yaw = mc.player.rotationYaw;
            if ((forward == 0.0D) && (strafe == 0.0D))
            {
                mc.player.getRidingEntity().motionX = 0.0D;
                mc.player.getRidingEntity().motionZ = 0.0D;
            }
            else
            {
                if (forward != 0.0D)
                {
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

                if (antiStuck.getValue() && mc.world.getChunkFromChunkCoords((int) (mc.player.getRidingEntity().posX + motX) >> 4, (int) (mc.player.getRidingEntity().posZ + motZ) >> 4) instanceof EmptyChunk)
                    motX = motZ = 0;

                mc.player.getRidingEntity().motionX = motX;
                mc.player.getRidingEntity().motionZ = motZ;
            }
        }
    }

}
