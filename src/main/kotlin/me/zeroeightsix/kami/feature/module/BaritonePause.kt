package me.zeroeightsix.kami.feature.module

import baritone.api.BaritoneAPI
import baritone.api.process.IBaritoneProcess
import baritone.api.process.PathingCommand
import baritone.api.process.PathingCommandType
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.BaritoneIntegration
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.event.TickEvent

@Module.Info(
    name = "BaritonePause",
    category = Module.Category.MISC,
    description = "Pauses Baritone"
)
object BaritonePause : Module() {
    var process: BaritoneWrapper? = null

    init {
        hidden = true
        BaritoneIntegration {
            hidden = false
            process = BaritoneWrapper(BaritoneWrapper.PauseBaritoneProcess("${KamiMod.MODNAME} Pause Module"))
            var listener: Listener<TickEvent.InGame>? = null
            listener = Listener({
                BaritoneIntegration {
                    val mngr = BaritoneAPI.getProvider()?.primaryBaritone?.pathingControlManager
                    mngr?.let {
                        it.registerProcess(process?.process)
                        KamiMod.EVENT_BUS.unsubscribe(listener)
                    }
                }
            })
            KamiMod.EVENT_BUS.subscribe(listener)
        }
    }

    override fun onDisable() {
        process?.process?.isPaused = false
    }

    override fun onEnable() {
        process?.process?.isPaused = true
    }
}

// this wrapper is required in order to keep kami from crashing without baritone
class BaritoneWrapper(val process: PauseBaritoneProcess) {
    class PauseBaritoneProcess(
        val displayName: String,
        var isPaused: Boolean = false
    ) : IBaritoneProcess {
        override fun isActive(): Boolean = isPaused

        override fun onTick(p0: Boolean, p1: Boolean) =
            PathingCommand(null, PathingCommandType.REQUEST_PAUSE)

        override fun isTemporary() = true

        override fun onLostControl() {}

        override fun displayName0() = displayName
    }
}
