package me.zeroeightsix.kami.module.modules.movement;

import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.util.EntityUtil;
import net.minecraft.entity.item.EntityBoat;

/**
 * Created by 086 on 15/12/2017.
 */
@Module.Info(name = "BoatSpeed", category = Module.Category.MOVEMENT)
public class BoatSpeed extends Module {

    private Setting<Float> speed = register(Settings.f("Speed", .5f));
    private Setting<Float> opacity = register(Settings.f("Opacity", .5f));

    private static BoatSpeed INSTANCE;

    public BoatSpeed() {
        INSTANCE = this;
    }

    @Override
    public void onRender() {
        if (isDisabled()) return;
        EntityBoat boat = getBoat();
        if (boat == null) return;
        boat.rotationYaw = mc.player.rotationYaw;
        boat.updateInputs(false, false, false, false); // Make sure the boat doesn't turn etc (params: isLeftDown, isRightDown, isForwardDown, isBackDown)
    }

    @Override
    public void onUpdate() {
        EntityBoat boat = getBoat();
        if (boat == null) return;

        int angle;

        boolean forward = mc.gameSettings.keyBindForward.isKeyDown();
        boolean left = mc.gameSettings.keyBindLeft.isKeyDown();
        boolean right = mc.gameSettings.keyBindRight.isKeyDown();
        boolean back = mc.gameSettings.keyBindBack.isKeyDown();
        if (!(forward && back)) boat.motionY = 0;
        if (mc.gameSettings.keyBindJump.isKeyDown()) boat.motionY += speed.getValue() / 2f;

        if (!forward && !left && !right && !back) return;
        if (left && right) angle = forward ? 0 : back ? 180 : -1;
        else if (forward && back) angle = left ? -90 : (right ? 90 : -1);
        else {
            angle = left ? -90 : (right ? 90 : 0);
            if (forward) angle /= 2;
            else if (back) angle = 180 - (angle / 2);
        }

        if (angle == -1) return;
        float yaw = mc.player.rotationYaw + angle;
        boat.motionX = EntityUtil.getRelativeX(yaw) * speed.getValue();
        boat.motionZ = EntityUtil.getRelativeZ(yaw) * speed.getValue();
    }

    private EntityBoat getBoat() {
        if (mc.player.getRidingEntity() != null && mc.player.getRidingEntity() instanceof EntityBoat)
            return (EntityBoat) mc.player.getRidingEntity();
        return null;
    }

    public static float getOpacity() {
        return INSTANCE.opacity.getValue();
    }
}
