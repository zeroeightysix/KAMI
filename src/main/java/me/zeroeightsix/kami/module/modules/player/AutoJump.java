package me.zeroeightsix.kami.module.modules.player;

import me.zeroeightsix.kami.module.ModulePlay;
import me.zeroeightsix.kami.util.EntityUtil;

/**
 * Created by 086 on 24/12/2017.
 */
@ModulePlay.Info(name = "AutoJump", category = ModulePlay.Category.PLAYER, description = "Automatically jumps if possible")
public class AutoJump extends ModulePlay {

    @Override
    public void onUpdate() {
        if (mc.player.isInWater() || mc.player.isInLava()) {
            EntityUtil.updateVelocityY(mc.player, 0.1);
        }
        else if (mc.player.onGround) mc.player.jump();
    }

}
