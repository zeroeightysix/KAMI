package me.zeroeightsix.kami.target

import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.event.TickEvent.*
import me.zeroeightsix.kami.isFriend
import me.zeroeightsix.kami.mc
import me.zeroeightsix.kami.util.ResettableLazy
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.ChestBlockEntity
import net.minecraft.block.entity.EnderChestBlockEntity
import net.minecraft.block.entity.ShulkerBoxBlockEntity
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.decoration.ItemFrameEntity
import net.minecraft.entity.mob.AmbientEntity
import net.minecraft.entity.mob.Angerable
import net.minecraft.entity.mob.Monster
import net.minecraft.entity.mob.WaterCreatureEntity
import net.minecraft.entity.passive.IronGolemEntity
import net.minecraft.entity.passive.PassiveEntity
import net.minecraft.entity.passive.WolfEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.vehicle.AbstractMinecartEntity
import net.minecraft.inventory.Inventory

private fun isPassive(e: Entity): Boolean {
    if (e is Monster || (e is WolfEntity && e.angryAt != mc.player?.uuid)) return false
    return if (e is PassiveEntity || e is AmbientEntity || e is WaterCreatureEntity) true else e is IronGolemEntity && e.target === null
}

private fun isHostile(e: Entity) = e is Monster || (e is Angerable && e.angryAt == mc.player?.uuid)

private operator fun <T> ((T) -> Boolean).not() = { t: T -> !this(t) }

private val allEntities
    get() = mc.world?.entities?.iterator()
private val allPlayers
    get() = mc.world?.players?.iterator()

private val allBlockEntities
    get() = mc.world?.blockEntities?.iterator()

private fun <T> emptyIterator() = emptyList<T>().iterator()

interface CategorisedTargetProvider<T> {
    val provider: ResettableLazy<Iterator<T>?>
    val belongsFunc: (T) -> Boolean

    fun invalidate() = provider.invalidate()
    fun iterator() = provider.value
}

/**
 * Returns an iterator over this [Iterator] that evaluates filtered elements only when requested
 */
private fun <T> Iterator<T>.filterLazily(filter: (T) -> Boolean): Iterator<T> = object : Iterator<T> {
    private val inner = this@filterLazily

    private var next: T? = null

    init {
        // Okay. We lied. This iterator actually evaluates the next element ahead of time - if we didn't, we'd have an unstable `hasNext` method.
        this.toNextFiltered()
    }

    /**
     * Calls [Iterator.next] on the underlying iterator until the next element that passes the filter is found. After, sets the [next] field to the element found, or `null` if none.
     */
    private fun toNextFiltered() {
        var n: T? = null
        while (inner.hasNext() && (n == null || !filter(n)))
            n = inner.next()
        this.next = n
    }

    override fun hasNext(): Boolean {
        return next != null
    }

    override fun next(): T {
        return next?.also {
            toNextFiltered()
        } ?: throw NoSuchElementException()
    }
}

@Suppress("unused")
enum class EntityCategory(
    val _belongsFunc: (Entity) -> Boolean,
    internal val baseIterable: () -> Iterator<Entity>?,
    /**
     * Player targets work on the pre-filtered set of players minecraft provides.
     * That means their `belongsFunc` will produce false positives when tested against non-player entities.
     * This method is used to identify whether or not an entity belongs to the base collection the target uses.
     */
    internal val genericBaseBelongsFunc: (Entity) -> Boolean = { true }
) : CategorisedTargetProvider<Entity> {
    NONE({ false }, ::emptyIterator, { false }),
    LIVING({ it is LivingEntity }, ::allEntities),
    NOT_LIVING({ it !is LivingEntity }, ::allEntities),
    PASSIVE(::isPassive, LIVING::iterator),
    HOSTILE(::isHostile, LIVING::iterator),
    ALL_PLAYERS({ true }, ::allPlayers, { it is PlayerEntity }),
    FRIENDLY_PLAYERS({ (it as PlayerEntity).isFriend() }, ALL_PLAYERS::iterator, ALL_PLAYERS.genericBaseBelongsFunc),
    NONFRIENDLY_PLAYERS(
        { !(it as PlayerEntity).isFriend() },
        ALL_PLAYERS::iterator,
        ALL_PLAYERS.genericBaseBelongsFunc
    ),
    MINECARTS({ it is AbstractMinecartEntity }, ::allEntities),
    ITEM_FRAMES({ it is ItemFrameEntity }, ::allEntities);

    override val belongsFunc: (Entity) -> Boolean = { this.genericBaseBelongsFunc(it) && this._belongsFunc(it) }

    override val provider = ResettableLazy {
        this.baseIterable()
            ?.filterLazily(this._belongsFunc)
    }
}

@Suppress("unused")
enum class BlockEntityCategory(
    override val belongsFunc: (BlockEntity) -> Boolean,
    internal val baseCollection: () -> Iterator<BlockEntity>?
) : CategorisedTargetProvider<BlockEntity> {
    NONE({ false }, ::emptyIterator),
    ALL_BLOCK_ENTITIES({ true }, ::allBlockEntities),
    CONTAINERS({ it is Inventory }, ::allBlockEntities),
    CHESTS({ it is ChestBlockEntity }, ::allBlockEntities),
    ENDER_CHESTS({ it is EnderChestBlockEntity }, ::allBlockEntities),
    SHULKERS({ it is ShulkerBoxBlockEntity }, CONTAINERS::iterator);

    override val provider = ResettableLazy {
        this.baseCollection()?.filterLazily(belongsFunc)
    }
}

@Suppress("unused") // Yes, it is unused, but the initializer does happen!
private object CategorisedTargets {
    init {
        Listener<InGame>({
            EntityCategory.values().forEach(CategorisedTargetProvider<*>::invalidate)
            BlockEntityCategory.values().forEach(CategorisedTargetProvider<*>::invalidate)
        }).also {
            KamiMod.EVENT_BUS.subscribe(it)
        }
    }
}