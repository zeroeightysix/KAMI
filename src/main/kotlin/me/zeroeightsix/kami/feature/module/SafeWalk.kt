package me.zeroeightsix.kami.feature.module

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.ClipAtLedgeEvent

@Module.Info(
    name = "SafeWalk",
    category = Module.Category.MOVEMENT,
    description = "Keeps you from walking off edges"
)
object SafeWalk : Module() {

    @EventHandler
    val clipListener = Listener<ClipAtLedgeEvent>({
        it.clip = true
    })

}
