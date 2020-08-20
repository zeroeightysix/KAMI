package me.zeroeightsix.kami.feature.module.render;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.ApplyFogEvent;
import me.zeroeightsix.kami.feature.module.Module;

@Module.Info(name = "AntiFog", description = "Disables or reduces fog", category = Module.Category.RENDER)
public class AntiFog extends Module {

    @EventHandler
    public Listener<ApplyFogEvent> applyFogListener = new Listener<>(event -> {
        event.cancel();
    });

}
