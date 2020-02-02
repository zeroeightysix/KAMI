package me.zeroeightsix.kami.module.modules.misc;

import me.zeroeightsix.kami.module.ModulePlay;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;

/**
 * Created by 086 on 8/04/2018.
 */
@ModulePlay.Info(name = "NoEntityTrace", category = ModulePlay.Category.MISC, description = "Blocks entities from stopping you from mining")
public class NoEntityTrace extends ModulePlay {

    private Setting<TraceMode> mode = register(Settings.e("Mode", TraceMode.DYNAMIC));

    private static NoEntityTrace INSTANCE;

    public NoEntityTrace() {
        NoEntityTrace.INSTANCE = this;
    }

    public static boolean shouldBlock() {
        return INSTANCE.isEnabled() && (INSTANCE.mode.getValue() == TraceMode.STATIC || mc.interactionManager.isBreakingBlock());
    }

    private enum TraceMode {
        STATIC, DYNAMIC
    }
}
