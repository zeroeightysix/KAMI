package me.zeroeightsix.kami.feature.module

import glm_.func.common.clamp
import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting
import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.BaritoneIntegration
import me.zeroeightsix.kami.event.CameraUpdateEvent
import me.zeroeightsix.kami.event.TickEvent
import me.zeroeightsix.kami.event.UpdateLookEvent
import me.zeroeightsix.kami.mixin.client.IEntity
import me.zeroeightsix.kami.mixin.extend.ipos
import me.zeroeightsix.kami.mixin.extend.setRenderHand
import me.zeroeightsix.kami.mixin.extend.setRotation
import me.zeroeightsix.kami.util.interpolated
import me.zeroeightsix.kami.util.plus
import net.minecraft.client.input.Input
import net.minecraft.client.input.KeyboardInput
import net.minecraft.util.math.Vec3d

@Module.Info(
    name = "Freecam",
    category = Module.Category.PLAYER,
    description = "Leave your body and transcend into the realm of the gods"
)
object Freecam : Module() {
    @Setting
    private var speed: @Setting.Constrain.Range(min = 0.0, max = 5.0, step = 0.1) Float = 2f

    @Setting(comment = "Whether or not the real player should still respond to inputs")
    private var blockInputs = true

    var pos: Vec3d = Vec3d.ZERO
    var yaw: Float = 0f
    var pitch: Float = 0f
    private var velocity: Vec3d = Vec3d.ZERO

    val input by lazy { KeyboardInput(mc.options) }
    var inputCache: Input? = null
    private val noInput = object : Input() {
        override fun tick(slowDown: Boolean) {
            this.movementSideways = 0f
            this.movementForward = 0f
        }
    }

    private fun disablePlayerInput() {
        inputCache = mc.player?.input
        mc.player?.input = noInput
    }

    override fun onEnable() {
        with(mc.player!!) {
            Freecam.pos = Vec3d(pos.x, pos.y + getEyeHeight(pose), pos.z)
            Freecam.yaw = yaw
            Freecam.pitch = pitch
        }

        mc.gameRenderer.setRenderHand(false)

        disablePlayerInput()
    }

    override fun onDisable() {
        mc.gameRenderer.setRenderHand(true)

        inputCache?.let {
            mc.player?.input = it
        }
    }

    @EventHandler
    val updateListener = Listener<CameraUpdateEvent>({
        with(it.camera) {
            setRotation(Freecam.yaw, Freecam.pitch)
            ipos = Freecam.pos.interpolated(mc.tickDelta.toDouble(), velocity)
        }
    })

    @EventHandler
    val tickListener = Listener<TickEvent.InGame>({
        this.input.tick(false)
        val movement = this.input.movementInput

        val velocity = IEntity.movementInputToVelocity(
            Vec3d(movement.x.toDouble(), 0.0, movement.y.toDouble()),
            speed,
            yaw
        ).add(0.0, ((mc.options.keyJump.isPressed * speed) - (mc.options.keySneak.isPressed * speed)).toDouble(), 0.0)
        this.pos += velocity
        this.velocity = velocity

        BaritoneIntegration {
            // After baritone completes a task, it reverts the input to a standard minecraft one.
            // We want to set this to the freecam one again.
            if (it.player.input?.javaClass == KeyboardInput::class.java) {
                disablePlayerInput()
            }
        }
    })

    @EventHandler
    val updateLookListener = Listener<UpdateLookEvent>({
        yaw += it.deltaX.toFloat() * 0.15f
        pitch += it.deltaY.toFloat() * 0.15f
        pitch = pitch.clamp(-90f, 90f)

        if (blockInputs) it.cancel()
    })

    operator fun Boolean.times(times: Float) = if (this) {
        times
    } else 0f
}
