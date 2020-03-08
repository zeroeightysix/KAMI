package me.zeroeightsix.kami.feature.module.misc;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.events.TickEvent;
import me.zeroeightsix.kami.feature.module.Module;

/**
 * Created by 086 on 8/04/2018.
 */
@Module.Info(name = "AntiWeather", description = "Removes rain from your world", category = Module.Category.MISC)
public class AntiWeather extends Module {

    @EventHandler
    private Listener<TickEvent.Client.InGame> updateListener = new Listener<>(event -> {
        if (isDisabled()) return;
        if (mc.world.isRaining())
            mc.world.setRainGradient(0);
    });

}
