package me.zeroeightsix.kami.feature.module.player;

import me.zeroeightsix.kami.feature.module.Module;

/**
 * Created by 086 on 9/04/2018.
 */
@Module.Info(name = "TpsSync", description = "Synchronizes some actions with the server TPS", category = Module.Category.PLAYER)
public class TpsSync extends Module {

    private static TpsSync INSTANCE;

    public TpsSync() {
        INSTANCE = this;
    }

    public static boolean isSync() {
        return INSTANCE.isEnabled();
    }

}
