package me.zeroeightsix.kami.module.modules.movement;

import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.util.math.MathHelper;

/**
 * Created by 086 on 11/04/2018.
 */
@Module.Info(name = "ElytraFlight", description = "Allows infinite elytra flying", category = Module.Category.MOVEMENT)
public class ElytraFlight extends Module {

    private Setting<ElytraFlightMode> mode = register(Settings.e("Mode", ElytraFlightMode.BOOST));

    @Override
    public void onUpdate() {
        if (!mc.player.isElytraFlying()) return;
        switch (mode.getValue()) {
            case BOOST:
                if(mc.player.isInWater())
                {
                    mc.getConnection()
                            .sendPacket(new CPacketEntityAction(mc.player,
                                    CPacketEntityAction.Action.START_FALL_FLYING));
                    return;
                }

                if(mc.gameSettings.keyBindJump.isKeyDown())
                    mc.player.motionY += 0.08;
                else if(mc.gameSettings.keyBindSneak.isKeyDown())
                    mc.player.motionY -= 0.04;

                if(mc.gameSettings.keyBindForward.isKeyDown()) {
                    float yaw = (float)Math
                            .toRadians(mc.player.rotationYaw);
                    mc.player.motionX -= MathHelper.sin(yaw) * 0.05F;
                    mc.player.motionZ += MathHelper.cos(yaw) * 0.05F;
                }else if(mc.gameSettings.keyBindBack.isKeyDown()) {
                    float yaw = (float)Math
                            .toRadians(mc.player.rotationYaw);
                    mc.player.motionX += MathHelper.sin(yaw) * 0.05F;
                    mc.player.motionZ -= MathHelper.cos(yaw) * 0.05F;
                }
                break;
            case FLY:
                mc.player.capabilities.isFlying = true;
        }
    }

    @Override
    protected void onDisable() {
        if (mc.player.capabilities.isCreativeMode) return;
        mc.player.capabilities.isFlying = false;
    }

    private enum ElytraFlightMode {
        BOOST, FLY,
    }

}
