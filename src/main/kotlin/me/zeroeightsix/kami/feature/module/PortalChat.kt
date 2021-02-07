package me.zeroeightsix.kami.feature.module

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.CloseScreenInPortalEvent

/**
 * @see me.zeroeightsix.kami.mixin.client.MixinClientPlayerEntity
 */
@Module.Info(name = "PortalChat", category = Module.Category.MISC, description = "Allows you to open GUIs in portals")
class PortalChat : Module() {
    @EventHandler
    var portalEventListener = Listener<CloseScreenInPortalEvent>(
        { event -> event.cancel() })
}