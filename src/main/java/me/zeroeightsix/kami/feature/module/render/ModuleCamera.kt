package me.zeroeightsix.kami.feature.module.render

import me.zeroeightsix.kami.feature.module.Module
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.setting.Settings

/**
 * Created by 086 on 11/12/2017.
 */
@Module.Info(
    name = "Camera",
    category = Module.Category.RENDER,
    description = "Allows modification of the camera behaviour"
)
object ModuleCamera : Module() {

    val desiredDistance: Setting<Double> = register(
        Settings.doubleBuilder("Distance from player")
            .withValue(4.0)
            .withMinimum(0.0)
            .withMaximum(50.0)
            .build() as Setting<Double>
    )
    
    val clip: Setting<Boolean> = register(Settings.b("Clip through blocks", true))
    
}