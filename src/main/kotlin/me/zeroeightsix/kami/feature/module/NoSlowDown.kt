package me.zeroeightsix.kami.feature.module

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.EntityVelocityMultiplierEvent
import me.zeroeightsix.kami.event.InputUpdateEvent

/**
 * Created by 086 on 15/12/2017.
 */
@Module.Info(
    name = "NoSlowDown",
    category = Module.Category.MOVEMENT
)
object NoSlowDown : Module() {
    @EventHandler
    private val inputUpdateEventListener =
        Listener(
            EventHook { event: InputUpdateEvent ->
                //
                // InputUpdateEvent is called just before the player is slowed down @see EntityPlayerSP.onLivingUpdate)
                // We'll abuse this fact, and multiply moveStrafe and moveForward by 5 to nullify the *0.2f hardcoded by mojang.
                //

                // Check if the player should be slowed down or not
                if (mc.player!!.isUsingItem && !mc.player!!.isRiding) {
                    event.newState.movementSideways *= 5f
                    event.newState.movementForward *= 5f
                }
            }
        )

    @EventHandler
    private val entityVelocityMultiplierEventListener =
        Listener(
            EventHook { event: EntityVelocityMultiplierEvent ->
                if (event.entity === mc.player) event.multiplier = 1f
            }
        )
}
