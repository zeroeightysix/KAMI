package me.zeroeightsix.kami.module.modules.render;

import me.zero.alpine.listener.Listener;
import me.zero.alpine.type.Cancellable;
import me.zeroeightsix.kami.event.events.CameraHurtEvent;
import me.zeroeightsix.kami.module.ModulePlay;

/**
 * @author 086
 */
@ModulePlay.Info(name = "NoHurtCam", category = ModulePlay.Category.RENDER, description = "Disables the 'hurt' camera effect")
public class NoHurtCam extends ModulePlay {

    public Listener<CameraHurtEvent> eventListener = new Listener<>(Cancellable::cancel);

}
