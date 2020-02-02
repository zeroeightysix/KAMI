package me.zeroeightsix.kami.module.modules.movement;

import me.zeroeightsix.kami.module.ModulePlay;

/**
 * Created by 086 on 23/08/2017.
 */
@ModulePlay.Info(name = "Sprint", description = "Automatically makes the player sprint", category = ModulePlay.Category.MOVEMENT)
public class Sprint extends ModulePlay {

    @Override
    public void onUpdate() {
        try {
            if (!mc.player.horizontalCollision && mc.player.forwardSpeed > 0)
                mc.player.setSprinting(true);
            else
                mc.player.setSprinting(false);
        } catch (Exception ignored) {}
    }

}
