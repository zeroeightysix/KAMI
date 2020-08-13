package me.zeroeightsix.kami.feature.module.render;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.CameraHurtEvent;
import me.zeroeightsix.kami.feature.module.Module;

/**
 * @author 086
 */
@Module.Info(name = "NoHurtCam", category = Module.Category.RENDER, description = "Disables the 'hurt' camera effect")
public class NoHurtCam extends Module {

    @EventHandler
    public Listener<CameraHurtEvent> eventListener = new Listener<>(cameraHurtEvent -> cameraHurtEvent.cancel());

}
