package me.zeroeightsix.kami.feature.module

import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting

/**
 * @see me.zeroeightsix.kami.mixin.client.MixinClientPlayNetworkHandler
 */
@Module.Info(
    name = "SignSpammer",
    category = Module.Category.MISC,
    description = "Automatically fills newly placed signs"
)
object SignSpammer : Module() {
    @Setting
    var line1 = ""
    @Setting
    var line2 = ""
    @Setting
    var line3 = ""
    @Setting
    var line4 = ""
}
