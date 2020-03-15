package me.zeroeightsix.kami.event.events

import me.zeroeightsix.kami.event.KamiEvent
import net.minecraft.entity.Entity

class EntityJoinWorldEvent(val id: Int, val entity: Entity) : KamiEvent()