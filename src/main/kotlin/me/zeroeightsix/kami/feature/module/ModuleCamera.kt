package me.zeroeightsix.kami.feature.module

import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting

@Module.Info(
    name = "Camera",
    category = Module.Category.RENDER,
    description = "Allows modification of the camera behaviour"
)
object ModuleCamera : Module() {

    @Setting(name = "Distance from player")
    var desiredDistance: @Setting.Constrain.Range(
        min = 0.0,
        max = 50.0, /* TODO: Remove when kotlin bug fixed */
        step = java.lang.Double.MIN_VALUE
    ) Double = 4.0;

    @Setting(name = "Clip through blocks")
    var clip: Boolean = true;

}
