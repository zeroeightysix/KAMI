package me.zeroeightsix.kami.feature.module

import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting
import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.CameraUpdateEvent
import me.zeroeightsix.kami.event.InputUpdateEvent
import me.zeroeightsix.kami.interpolated
import me.zeroeightsix.kami.mixin.client.IEntity
import me.zeroeightsix.kami.mixin.extend.setPos
import me.zeroeightsix.kami.mixin.extend.update
import me.zeroeightsix.kami.plus
import net.minecraft.client.input.Input
import net.minecraft.util.math.Vec3d

/**
 * Created by 086 on 22/12/2017.
 */
@Module.Info(
    name = "Freecam",
    category = Module.Category.PLAYER,
    description = "Leave your body and trascend into the realm of the gods"
)
object Freecam : Module() {
    @Setting
    private var speed: @Setting.Constrain.Range(min = 0.0, max = 5.0, step = 0.1) Float = 2f

    val EMPTY_INPUT = Input()

    var pos: Vec3d = Vec3d.ZERO
    var velocity: Vec3d = Vec3d.ZERO

    override fun onEnable() {
        with(mc.player!!.pos) {
            pos = Vec3d(x, y, z)
        }
    }

    @EventHandler
    val updateListener = Listener<CameraUpdateEvent>(EventHook {
        with(it.camera) {
            setPos(Freecam.pos.interpolated(mc.tickDelta.toDouble(), velocity))
        }
    })

    @EventHandler
    val inputUpdateListener = Listener<InputUpdateEvent>(EventHook {
        val input = it.newState.movementInput

        val velocity = IEntity.movementInputToVelocity(
            Vec3d(input.x.toDouble(), 0.0, input.y.toDouble()),
            speed,
            mc.player!!.yaw
        ).add(0.0, ((mc.options.keyJump.isPressed * speed) - (mc.options.keySneak.isPressed * speed)).toDouble(), 0.0)
        this.pos += velocity
        this.velocity = velocity

        it.newState.update(EMPTY_INPUT)
    })

    operator fun Boolean.times(times: Float) = if (this) {
        times
    } else 0f

}
