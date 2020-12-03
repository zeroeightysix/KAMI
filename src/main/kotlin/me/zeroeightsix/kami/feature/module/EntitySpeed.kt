package me.zeroeightsix.kami.feature.module

import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting
import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.CanBeControlledEvent
import me.zeroeightsix.kami.event.RenderGuiEvent
import me.zeroeightsix.kami.event.TickEvent
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
    private val updateListener = Listener<TickEvent.InGame>(
        {
            val player = it.player
            if (player.vehicle != null) {
                when (val riding = player.vehicle) {
                    is BoatEntity, is PigEntity, is HorseBaseEntity, is StriderEntity -> {
                        val input = player.input.movementInput

                        val movement = IEntity.movementInputToVelocity(
                            Vec3d(input.x.toDouble(), 0.0, input.y.toDouble()),
                            speed.toFloat(),
                            player.yaw
                        ).add(0.0, riding.velocity.y, 0.0)

                        riding.velocity = movement

                        steerEntity(riding)
                    }
                }
            }
        }
    )

    @EventHandler
    var eventListener = Listener(
        EventHook<CanBeControlledEvent> { it.canBeSteered = true }
    )

    private fun steerEntity(entity: Entity) {
        val forward = mc.options.keyForward.isPressed
        val left = mc.options.keyLeft.isPressed
        val right = mc.options.keyRight.isPressed
        val back = mc.options.keyBack.isPressed
        val jump = mc.options.keyJump.isPressed

        // make sure boat doesn't sink
        if (entity is BoatEntity && !(forward && back) && flight) {
            EntityUtil.updateVelocityY(entity, 0.025)
        }

        if (!(forward || left || right || back || jump)) return

        if (!flight) {
            EntityUtil.updateVelocityY(entity, -0.4)
        } else {
            if (jump) {
                EntityUtil.updateVelocityY(entity, speed)
            } else if (forward || back) if (wobble) mc.player?.age?.toDouble()
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

    @EventHandler
    var renderListener =
        Listener<RenderGuiEvent>({
            val boat = boat ?: return@Listener
            boat.yaw = mc.player?.yaw!!
            boat.setInputs(
                false,
                false,
                false,
                false
            ) // Make sure the boat doesn't turn etc (params: isLeftDown, isRightDown, isForwardDown, isBackDown)
        })

    private val boat: BoatEntity?
        get() = if (mc.player?.vehicle != null && mc.player?.vehicle is BoatEntity) mc.player?.vehicle as BoatEntity? else null
}
