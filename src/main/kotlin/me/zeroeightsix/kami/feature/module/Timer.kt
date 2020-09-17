package me.zeroeightsix.kami.feature.module

import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting
import me.zeroeightsix.kami.mixin.client.MixinRenderTickCounter

/**
 * @see MixinRenderTickCounter
 */
@Module.Info(name = "Timer", category = Module.Category.MISC, description = "Modifies the speed the game runs at")
object Timer : Module() {

    @Setting
    private var speed: @Setting.Constrain.Range(min = 0.1, max = 10.0, step = 0.1) Float = 2f

    fun getSpeedModifier() = if (enabled) {
        speed
    } else {
        1f
    }
}
