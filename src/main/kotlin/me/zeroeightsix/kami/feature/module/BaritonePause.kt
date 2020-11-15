package me.zeroeightsix.kami.feature.module

import baritone.api.BaritoneAPI
import baritone.api.process.IBaritoneProcess
import baritone.api.process.PathingCommand
import baritone.api.process.PathingCommandType
import me.zero.alpine.listener.EventHandler
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
    var process: PauseBaritoneProcess? = null
    var registeredProcess = false

    init {
        hidden = true
        BaritoneIntegration {
            hidden = false
            process = PauseBaritoneProcess("${KamiMod.MODNAME} Pause Module")
        }
    }

    @EventHandler
    val onTick = Listener<TickEvent.InGame>({
        tryInitProcess()
    })

    private fun tryInitProcess() {
        if (registeredProcess) return
        BaritoneIntegration {
            val mngr = BaritoneAPI.getProvider()?.primaryBaritone?.pathingControlManager
            mngr?.let {
                it.registerProcess(process)
                registeredProcess = true
            }
        }
    }

    override fun onDisable() {
        process?.isPaused = false
    }

    override fun onEnable() {
        process?.isPaused = true
    }
}

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
