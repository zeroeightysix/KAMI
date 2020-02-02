package me.zeroeightsix.kami.module.modules.player;

import me.zeroeightsix.kami.module.ModulePlay;

/**
 * Created by 086 on 9/04/2018.
 */
@ModulePlay.Info(name = "TpsSync", description = "Synchronizes some actions with the server TPS", category = ModulePlay.Category.PLAYER)
public class TpsSync extends ModulePlay {

    private static TpsSync INSTANCE;

    public TpsSync() {
        INSTANCE = this;
    }

    public static boolean isSync() {
        return INSTANCE.isEnabled();
    }

}
