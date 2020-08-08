package me.zeroeightsix.kami.feature.module.misc

import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting
import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.events.TargetEntityEvent
import me.zeroeightsix.kami.feature.module.Module

/**
 * Created by 086 on 8/04/2018.
 */
@Module.Info(
    name = "NoEntityTrace",
    category = Module.Category.MISC,
    description = "Blocks entities from stopping you from mining"
)
object NoEntityTrace : Module() {
    @Setting
    var traceMode = TraceMode.DYNAMIC

    enum class TraceMode {
        STATIC, DYNAMIC
    }

    @EventHandler
    val targetListener = Listener(EventHook<TargetEntityEvent> {
        if (traceMode == TraceMode.STATIC || mc.interactionManager?.isBreakingBlock == true)
            it.trace = null
    })
}
