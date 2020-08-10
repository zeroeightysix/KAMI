package me.zeroeightsix.kami.feature.module.misc;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.events.RenderWeatherEvent;
import me.zeroeightsix.kami.feature.module.Module;

/**
 * Created by 086 on 8/04/2018.
 */
@Module.Info(name = "AntiRain", description = "Removes rain from your world", category = Module.Category.MISC)
public class AntiRain extends Module {

    @EventHandler
    private Listener<RenderWeatherEvent> renderWeatherEventListener = new Listener<>(event -> {
        event.cancel();
    });

}
