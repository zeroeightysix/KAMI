package me.zeroeightsix.kami.module.modules.misc;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.events.CloseScreenInPortalEvent;
import me.zeroeightsix.kami.module.Module;

/**
 * Created by 086 on 12/12/2017.
 * @see me.zeroeightsix.kami.mixin.client.MixinClientPlayerEntity
 */
@Module.Info(name = "PortalChat", category = Module.Category.MISC, description = "Allows you to open GUIs in portals")
public class PortalChat extends Module {

    @EventHandler
    public Listener<CloseScreenInPortalEvent> closeScreenInPortalEventListener = new Listener<>(event -> event.cancel());

}
