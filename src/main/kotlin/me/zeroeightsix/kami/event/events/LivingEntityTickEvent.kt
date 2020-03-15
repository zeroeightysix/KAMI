package me.zeroeightsix.kami.event.events

import me.zeroeightsix.kami.event.KamiEvent
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity

class LivingEntityTickEvent(private val entity: LivingEntity) : KamiEvent() {
    fun getEntity(): Entity {
        return entity
    }

}