package me.zeroeightsix.kami.feature.module

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting
import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Settings
import me.zeroeightsix.kami.event.events.CanBeSteeredEvent
import me.zeroeightsix.kami.event.events.RenderHudEvent
import me.zeroeightsix.kami.event.events.TickEvent
import me.zeroeightsix.kami.util.EntityUtil
import net.minecraft.entity.Entity
import net.minecraft.entity.passive.HorseBaseEntity
import net.minecraft.entity.passive.HorseEntity
import net.minecraft.entity.passive.PigEntity
import net.minecraft.entity.vehicle.BoatEntity
import net.minecraft.world.chunk.EmptyChunk
import kotlin.math.cos
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
    @Setting(name = "Boat opacity")
    var opacity = .5f

    @EventHandler
    private val updateListener =
        Listener(
            EventHook<TickEvent.Client.InGame> {
                if (mc.world != null && mc.player?.vehicle != null) {
                    val riding = mc.player!!.vehicle
                    if (riding is PigEntity || riding is HorseBaseEntity) {
                        steerEntity(riding)
                    } else if (riding is BoatEntity) {
                        steerBoat(boat)
                    }
                }
            }
        )

    private fun steerEntity(entity: Entity) {
        if (!flight) {
            EntityUtil.updateVelocityY(entity, -0.4)
        }
        if (flight) {
            if (mc.options.keyJump.isPressed) {
                EntityUtil.updateVelocityY(entity, speed)
            } else if (mc.options.keyForward.isPressed || mc.options.keyBack.isPressed) if (wobble) mc.player?.age?.toDouble()?.let { sin(it) } else 0.0?.let {
                EntityUtil.updateVelocityY(
                    entity,
                    it
                )
            }
        }
        moveForward(
            entity,
            speed * 3.8
        )
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

    @EventHandler
    var eventListener = Listener(
        EventHook { event: CanBeSteeredEvent -> event.canBeSteered = event.canBeSteered || isEnabled() }
    )

    private val boat: BoatEntity?
        get() = if (mc.player?.vehicle != null && mc.player!!.vehicle is BoatEntity) mc.player!!.vehicle as BoatEntity? else null

    private fun moveForward(entity: Entity?, speed: Double) {
        if (entity != null) {
            val movementInput = mc.player?.input
            var forward = movementInput?.movementForward?.toDouble()
            var strafe = movementInput?.movementSideways?.toDouble()
            val movingForward = forward != 0.0
            val movingStrafe = strafe != 0.0
            var yaw = mc.player?.yaw
            if (!movingForward && !movingStrafe) {
                setEntitySpeed(entity, 0.0, 0.0)
            } else {
                if (forward != 0.0 && strafe != null && yaw != null) {
                    if (strafe != null) {
                        if (yaw != null) {
                            if (forward != null) {
                                if (strafe > 0.0) {
                                    yaw += (if (forward > 0.0) -45 else 45).toFloat()
                                } else if (forward != null) {
                                    if (strafe < 0.0) {
                                        yaw += (if (forward > 0.0) 45 else -45).toFloat()
                                    }
                                }
                            }
                        }
                    }
                    strafe = 0.0
                    if (forward != null) {
                        forward = if (forward > 0.0) {
                            1.0
                        } else {
                            -1.0
                        }
                    }
                }
                var motX = 0.0
                var motZ = 0.0
                if (forward != null && yaw != null && strafe != null) {
                    motX =
                        forward * speed * cos(Math.toRadians(yaw + 90.0f.toDouble())) + strafe * speed * sin(
                            Math.toRadians(yaw + 90.0f.toDouble())
                        )
                    motZ =
                        forward * speed * sin(Math.toRadians(yaw + 90.0f.toDouble())) - strafe * speed * cos(
                            Math.toRadians(yaw + 90.0f.toDouble())
                        )
                }
                if (isBorderingChunk(
                        entity,
                        motX,
                        motZ
                    )
                ) {
                    motZ = 0.0
                    motX = motZ
                }
                setEntitySpeed(entity, motX, motZ)
            }
        }
    }

    private fun setEntitySpeed(
        entity: Entity,
        motX: Double,
        motZ: Double
    ) {
        entity.setVelocity(motX, entity.velocity.y, motZ)
    }

    private fun isBorderingChunk(
        entity: Entity,
        motX: Double,
        motZ: Double
    ): Boolean {
        return antiStuck && mc.world?.getChunk(
            (entity.x + motX).toInt() shr 4,
            (entity.z + motZ).toInt() shr 4
        ) is EmptyChunk
    }

}