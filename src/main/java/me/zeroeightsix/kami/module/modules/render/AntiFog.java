package me.zeroeightsix.kami.module.modules.render;

import me.zeroeightsix.kami.module.ModulePlay;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;

/**
 * Created by 086 on 9/04/2018.
 */
@ModulePlay.Info(name = "AntiFog", description = "Disables or reduces fog", category = ModulePlay.Category.RENDER)
public class AntiFog extends ModulePlay {

    public static Setting<VisionMode> mode = Settings.e("Mode", VisionMode.NOFOG);
    private static AntiFog INSTANCE = new AntiFog();

    public AntiFog() {
        INSTANCE = this;
        register(mode);
    }

    public static boolean enabled() {
        return INSTANCE.isEnabled();
    }

    public enum VisionMode {
        NOFOG, AIR
    }

}
