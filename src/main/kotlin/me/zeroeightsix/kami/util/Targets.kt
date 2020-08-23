package me.zeroeightsix.kami.util

import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.event.TickEvent
import me.zeroeightsix.kami.isFriend
import me.zeroeightsix.kami.mc
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.mob.AmbientEntity
import net.minecraft.entity.mob.Angerable
import net.minecraft.entity.mob.HostileEntity
import net.minecraft.entity.mob.WaterCreatureEntity
import net.minecraft.entity.passive.AnimalEntity
import net.minecraft.entity.passive.IronGolemEntity
import net.minecraft.entity.passive.WolfEntity
import net.minecraft.entity.player.PlayerEntity

val invalidationListener = Listener<TickEvent.Client.InGame>(EventHook {
    Target.values().forEach { it.invalidate() }
}).also {
    KamiMod.EVENT_BUS.subscribe(it)
}

fun isPassive(e: Entity): Boolean {
    if (e is WolfEntity && e.angryAt != mc.player?.uuid) return false
    return if (e is AnimalEntity || e is AmbientEntity || e is WaterCreatureEntity) true else e is IronGolemEntity && e.target === null
}

fun isHostile(e: Entity) = e is HostileEntity || (e is Angerable && e.angryAt == mc.player?.uuid)

operator fun <T> ((T) -> Boolean).not() = { t: T -> !this(t) }

private fun allEntities(belongsFunc: (Entity) -> Boolean) = ResettableLazy {
    mc.world?.entities?.filter(belongsFunc)
}

enum class Target(private val provider: ResettableLazy<List<Entity>?>) {
    LIVING(allEntities { it is LivingEntity }),
    NOT_LIVING(allEntities { it !is LivingEntity }),
    PASSIVE(allEntities(::isPassive)),
    HOSTILE(allEntities(::isHostile)),
    ALL_PLAYERS(ResettableLazy { mc.world?.players }),
    FRIENDLY_PLAYERS(ResettableLazy { ALL_PLAYERS.entities?.filter { (it as PlayerEntity).isFriend() } }),
    NONFRIENDLY_PLAYERS(ResettableLazy { ALL_PLAYERS.entities?.filter { !(it as PlayerEntity).isFriend() } });

    fun invalidate() = provider.invalidate()
    val entities by provider
}

class Targets<T>(private val inner: Map<Target, T>) : Map<Target, T> by inner {
    val entities: MutableMap<Entity, T>
        get() = flat()

    private fun flat(): MutableMap<Entity, T> {
        val map = mutableMapOf<Entity, T>()
        for ((target, t) in this) {
            target.entities?.forEach { map[it] = t }
        }
        map.remove(mc.player) // We never want the player included in the entity list. Sorry bud.
        return map
    }
}
