package me.zeroeightsix.kami.module.modules.movement;

import me.zeroeightsix.kami.module.ModulePlay;

/**
 * Created by 086 on 11/10/2018.
 */
@ModulePlay.Info(name = "SafeWalk", category = ModulePlay.Category.MOVEMENT, description = "Keeps you from walking off edges")
public class SafeWalk extends ModulePlay {

    private static SafeWalk INSTANCE;

    public SafeWalk() {
        INSTANCE = this;
    }

    public static boolean shouldSafewalk() {
        return INSTANCE.isEnabled();
    }

}
