package me.zeroeightsix.kami.feature.module

import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting
import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.events.CanBeControlledEvent
import me.zeroeightsix.kami.event.events.RenderHudEvent
import me.zeroeightsix.kami.event.events.TickEvent
import me.zeroeightsix.kami.mixin.client.IEntity
import me.zeroeightsix.kami.util.EntityUtil
import net.minecraft.entity.Entity
import net.minecraft.entity.passive.HorseBaseEntity
import net.minecraft.entity.passive.HorseEntity
import net.minecraft.entity.passive.PigEntity
import net.minecraft.entity.passive.StriderEntity
import net.minecraft.entity.vehicle.BoatEntity
import net.minecraft.util.math.Vec3d
import kotlin.math.sin

/**
 * Created by 086 on 16/12/2017.
 */
@Module.Info(
    name = "EntitySpeed",
    category = Module.Category.MOVEMENT,
    description = "Abuse client-sided movement to shape sound barrier breaking rideables"
)
object EntitySpeed : Module() {

    @Setting
    private var speed = 1.0

    @Setting
    private var antiStuck = true

    @Setting
    private var flight = false

    @Setting
    private var wobble = true

    @EventHandler
    private val updateListener = Listener(
        EventHook<TickEvent.Client.InGame> {
            if (mc.world != null && mc.player?.vehicle != null) {
                when (val riding = mc.player!!.vehicle) {
                    is PigEntity, is HorseBaseEntity, is StriderEntity -> {
                        val player = mc.player!!
                        val input = player.input.movementInput

                        val movement = IEntity.movementInputToVelocity(
                            Vec3d(input.x.toDouble(), 0.0, input.y.toDouble()),
                            speed.toFloat(),
                            player.yaw
                        ).add(0.0, riding.velocity.y, 0.0)

                        riding.velocity = movement

                        steerEntity(riding)
                    }
                    is BoatEntity -> steerBoat(boat)
                }
            }
        }
    )

    @EventHandler
    var eventListener = Listener(
        EventHook<CanBeControlledEvent> { it.canBeSteered = true }
    )

    private fun steerEntity(entity: Entity) {
        if (!flight) {
            EntityUtil.updateVelocityY(entity, -0.4)
        } else {
            if (mc.options.keyJump.isPressed) {
                EntityUtil.updateVelocityY(entity, speed)
            } else if (mc.options.keyForward.isPressed || mc.options.keyBack.isPressed) if (wobble) mc.player?.age?.toDouble()
                ?.let { sin(it) } else 0.0.let {
                EntityUtil.updateVelocityY(
                    entity,
                    it
                )
            }
        }
        if (entity is HorseEntity) {
            entity.yaw = mc.player?.yaw!!
        }
    }

    private fun steerBoat(boat: BoatEntity?) {
        if (boat == null) return
        var angle: Int
        val forward = mc.options.keyForward.isPressed
        val left = mc.options.keyLeft.isPressed
        val right = mc.options.keyRight.isPressed
        val back = mc.options.keyBack.isPressed
        if (!(forward && back)) {
            EntityUtil.updateVelocityY(boat, 0.0)
        }
        if (mc.options.keyJump.isPressed) {
            boat.velocity = boat.velocity.add(0.0, speed / 2.0, 0.0)
        }
        if (!forward && !left && !right && !back) return
        if (left && right) angle = if (forward) 0 else if (back) 180 else -1 else if (forward && back) angle =
            if (left) -90 else if (right) 90 else -1 else {
            angle = if (left) -90 else if (right) 90 else 0
            if (forward) angle /= 2 else if (back) angle = 180 - angle / 2
        }
        if (angle == -1) return
        val yaw = mc.player?.yaw?.plus(angle)
        yaw?.let {
            boat.setVelocity(
                EntityUtil.getRelativeX(it) * speed,
                boat.velocity.y,
                EntityUtil.getRelativeZ(it) * speed
            )
        }
    }

    @EventHandler
    var renderListener =
        Listener(EventHook<RenderHudEvent> {
            val boat = boat ?: return@EventHook
            boat.yaw = mc.player?.yaw!!
            boat.setInputs(
                false,
                false,
                false,
                false
            ) // Make sure the boat doesn't turn etc (params: isLeftDown, isRightDown, isForwardDown, isBackDown)
        })

    private val boat: BoatEntity?
        get() = if (mc.player?.vehicle != null && mc.player!!.vehicle is BoatEntity) mc.player!!.vehicle as BoatEntity? else null

}
