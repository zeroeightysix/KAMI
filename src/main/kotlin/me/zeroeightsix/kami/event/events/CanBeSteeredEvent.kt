package me.zeroeightsix.kami.event.events

import me.zeroeightsix.kami.event.KamiEvent
import net.minecraft.entity.Entity

class CanBeSteeredEvent(val entity: Entity, var canBeSteered: Boolean) : KamiEvent() {}