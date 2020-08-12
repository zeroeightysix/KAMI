package me.zeroeightsix.kami.event.events

import me.zeroeightsix.kami.event.KamiEvent
import net.minecraft.entity.Entity

class EntityVelocityMultiplierEvent(val entity: Entity?, var multiplier: Float) : KamiEvent()
