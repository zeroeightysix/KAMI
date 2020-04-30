package me.zeroeightsix.kami.feature.module.render

import me.zeroeightsix.fiber.api.annotation.Setting
import me.zeroeightsix.fiber.api.annotation.Settings
import me.zeroeightsix.kami.feature.module.Module

/**
 * Created by 086 on 11/12/2017.
 */
@Module.Info(
    name = "Camera",
    category = Module.Category.RENDER,
    description = "Allows modification of the camera behaviour"
)
@Settings(onlyAnnotated = true)
object ModuleCamera : Module() {

    @Setting(name = "Distance from player")
    var desiredDistance: @Setting.Constrain.Range(min = 0.0, max = 50.0) Double = 4.0;
    @Setting(name = "Clip through blocks")
    var clip: Boolean = true;

}