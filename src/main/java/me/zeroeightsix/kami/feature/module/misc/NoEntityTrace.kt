package me.zeroeightsix.kami.feature.module.misc

import me.zeroeightsix.kami.feature.module.Module
import me.zeroeightsix.kami.setting.Settings

/**
 * Created by 086 on 8/04/2018.
 */
@Module.Info(
    name = "NoEntityTrace",
    category = Module.Category.MISC,
    description = "Blocks entities from stopping you from mining"
)
object NoEntityTrace : Module() {
    private val mode =
        register(Settings.e<TraceMode>("Mode", TraceMode.DYNAMIC))

    private enum class TraceMode {
        STATIC, DYNAMIC
    }

    @JvmStatic
    fun shouldBlock(): Boolean {
        return isEnabled() && (mode.value == TraceMode.STATIC || mc.interactionManager.isBreakingBlock)
    }
}