package me.zeroeightsix.kami.feature.module

import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting
import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.CameraUpdateEvent
import me.zeroeightsix.kami.mixin.extend.ipos
import me.zeroeightsix.kami.util.plus
import net.minecraft.util.math.Vec3d

@Module.Info(
    name = "Camera",
    category = Module.Category.RENDER,
    description = "Allows modification of the camera behaviour while in 3rd person mode"
)
object ModuleCamera : Module() {

    @Setting(name = "Distance from player")
    var desiredDistance: @Setting.Constrain.Range(
        min = 0.0,
        max = 50.0,
        step = 0.1
    ) Double = 4.0

    @Setting(name = "Shift Y")
    var shiftY: @Setting.Constrain.Range(
        min = -5.0,
        max = 10.0,
        step = 0.01
    ) Double = 0.0

    @Setting(name = "Clip through blocks")
    var clip: Boolean = true

    @EventHandler
    val updateCameraListener = Listener<CameraUpdateEvent>({
        with(it.camera) {
            if (!isThirdPerson) return@Listener
            ipos += Vec3d(0.0, shiftY, 0.0)
        }
    })

}
