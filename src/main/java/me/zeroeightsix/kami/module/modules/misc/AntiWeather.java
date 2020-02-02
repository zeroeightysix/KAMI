package me.zeroeightsix.kami.module.modules.misc;

import me.zeroeightsix.kami.module.ModulePlay;

/**
 * Created by 086 on 8/04/2018.
 */
@ModulePlay.Info(name = "AntiWeather", description = "Removes rain from your world", category = ModulePlay.Category.MISC)
public class AntiWeather extends ModulePlay {

    @Override
    public void onUpdate() {
        if (isDisabled()) return;
        if (mc.world.isRaining())
            mc.world.setRainGradient(0);
    }
}
