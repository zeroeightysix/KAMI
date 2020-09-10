package me.zeroeightsix.kami.feature.module

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.EntityVelocityMultiplierEvent

@Module.Info(
    name = "NoSlowDown",
    category = Module.Category.MOVEMENT
)
object NoSlowDown : Module() {
    @EventHandler
    private val entityVelocityMultiplierEventListener =
        Listener(
            { event: EntityVelocityMultiplierEvent ->
                if (event.entity === mc.player) event.multiplier = 1f
            }
        )
}
