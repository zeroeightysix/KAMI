package me.zeroeightsix.kami.util

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
import net.minecraft.entity.passive.IronGolemEntity
import net.minecraft.entity.passive.PassiveEntity
import net.minecraft.entity.passive.WolfEntity
import net.minecraft.entity.player.PlayerEntity

val invalidationListener = Listener<TickEvent.Client.InGame>({
    Target.values().forEach { it.invalidate() }
}).also {
    KamiMod.EVENT_BUS.subscribe(it)
}

fun isPassive(e: Entity): Boolean {
    if (e is WolfEntity && e.angryAt != mc.player?.uuid) return false
    return if (e is PassiveEntity || e is AmbientEntity || e is WaterCreatureEntity) true else e is IronGolemEntity && e.target === null
}

fun isHostile(e: Entity) = e is HostileEntity || (e is Angerable && e.angryAt == mc.player?.uuid)

operator fun <T> ((T) -> Boolean).not() = { t: T -> !this(t) }

private fun allEntities() = mc.world?.entities
private fun allPlayers() = mc.world?.players

enum class Target(
    val belongsFunc: (Entity) -> Boolean,
    internal val baseCollection: () -> Iterable<Entity>?,
    internal val genericBaseBelongsFunc: (Entity) -> Boolean = { true }
) {
    LIVING({ it is LivingEntity }, ::allEntities),
    NOT_LIVING({ it !is LivingEntity }, ::allEntities),
    PASSIVE(::isPassive, LIVING::entities),
    HOSTILE(::isHostile, LIVING::entities),
    ALL_PLAYERS({ true }, ::allPlayers, { it is PlayerEntity }),
    FRIENDLY_PLAYERS({ (it as PlayerEntity).isFriend() }, ALL_PLAYERS::entities, ALL_PLAYERS.genericBaseBelongsFunc),
    NONFRIENDLY_PLAYERS(
        { !(it as PlayerEntity).isFriend() },
        ALL_PLAYERS::entities,
        ALL_PLAYERS.genericBaseBelongsFunc
    );

    val provider = ResettableLazy {
        this.baseCollection()?.filter { this.belongsFunc(it) }
    }
    val entities by provider

    fun invalidate() = provider.invalidate()
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

    fun belongs(entity: Entity) =
        this.entries.find { it.key.genericBaseBelongsFunc(entity) && it.key.belongsFunc(entity) }?.value
}
