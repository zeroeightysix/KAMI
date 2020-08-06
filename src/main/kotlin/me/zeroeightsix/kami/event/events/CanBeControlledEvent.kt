package me.zeroeightsix.kami.event.events

import me.zeroeightsix.kami.event.KamiEvent
import net.minecraft.entity.Entity

class CanBeControlledEvent(val entity: Entity, var canBeSteered: Boolean?) : KamiEvent() {}
