package me.zeroeightsix.kami.feature.module.movement

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
    @JvmStatic
    fun shouldSafewalk(): Boolean {
        return isEnabled()
    }
}