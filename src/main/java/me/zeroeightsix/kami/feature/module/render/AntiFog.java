package me.zeroeightsix.kami.feature.module.render;

import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting;
import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Settings;
import me.zeroeightsix.kami.feature.module.Module;

/**
 * Created by 086 on 9/04/2018.
 */
@Module.Info(name = "AntiFog", description = "Disables or reduces fog", category = Module.Category.RENDER)
@Settings(onlyAnnotated = true)
public class AntiFog extends Module {

    @Setting
    public static VisionMode mode = VisionMode.NOFOG;
    private static AntiFog INSTANCE = new AntiFog();

    public AntiFog() {
        INSTANCE = this;
    }

    public static boolean enabled() {
        return INSTANCE.isEnabled();
    }

    public enum VisionMode {
        NOFOG, AIR
    }

}
