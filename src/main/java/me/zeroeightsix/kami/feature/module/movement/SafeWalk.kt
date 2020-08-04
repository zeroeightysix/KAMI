package me.zeroeightsix.kami.feature.module.movement

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.events.ClipAtLedgeEvent
import me.zeroeightsix.kami.feature.module.Module

/**
 * Created by 086 on 11/10/2018.
 */
@Module.Info(
    name = "SafeWalk",
    category = Module.Category.MOVEMENT,
    description = "Keeps you from walking off edges"
)
object SafeWalk : Module() {

    @EventHandler
    val clipListener = Listener<ClipAtLedgeEvent>(EventHook {
        it.clip = true
    })

}
