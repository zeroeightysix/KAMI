package me.zeroeightsix.kami.target

import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.event.TickEvent.InGame
import me.zeroeightsix.kami.isFriend
import me.zeroeightsix.kami.mc
import me.zeroeightsix.kami.util.ResettableLazy
import net.minecraft.block.Block
import net.minecraft.block.Fertilizable
import net.minecraft.block.OreBlock
import net.minecraft.block.SlabBlock
import net.minecraft.block.Waterloggable
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.ChestBlockEntity
import net.minecraft.block.entity.EnderChestBlockEntity
import net.minecraft.block.entity.ShulkerBoxBlockEntity
import net.minecraft.entity.Entity
import net.minecraft.entity.ItemEntity
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
import net.minecraft.item.AxeItem
import net.minecraft.item.BlockItem
import net.minecraft.item.HoeItem
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.item.Items
import net.minecraft.item.PickaxeItem
import net.minecraft.item.ShovelItem
import net.minecraft.item.SwordItem
import net.minecraft.item.ToolItem
import net.minecraft.item.Wearable

private fun isPassive(e: Entity): Boolean {
    if (e is Monster || (e is WolfEntity && e.angryAt != mc.player?.uuid)) return false
    return if (e is PassiveEntity || e is AmbientEntity || e is WaterCreatureEntity) true else e is IronGolemEntity && e.target === null
}

private fun isHostile(e: Entity) = e is Monster || (e is Angerable && e.angryAt == mc.player?.uuid)

private operator fun <T> ((T) -> Boolean).not() = { t: T -> !this(t) }

private val allEntities
    get() = mc.world?.entities
private val allPlayers
    get() = mc.world?.players

private val allBlockEntities
    get() = mc.world?.blockEntities

internal fun <T> emptyIterator() = emptyList<T>().iterator()

interface CategorisedTargetProvider<T> {
    val provider: ResettableLazy<List<T>?>
    val belongsFunc: (T) -> Boolean

    fun invalidate() = provider.invalidate()
    fun iterator() = provider.value
}

@Suppress("unused")
enum class EntityCategory(
    val _belongsFunc: (Entity) -> Boolean,
    internal val baseIterable: () -> Iterable<Entity>?,
    /**
     * Player targets work on the pre-filtered set of players minecraft provides.
     * That means their `belongsFunc` will produce false positives when tested against non-player entities.
     * This method is used to identify whether or not an entity belongs to the base collection the target uses.
     */
    internal val genericBaseBelongsFunc: (Entity) -> Boolean = { true }
) : CategorisedTargetProvider<Entity> {
    NONE({ false }, ::emptyList, { false }),
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
    DROPPED_ITEMS({ it is ItemEntity }, ::allEntities),
    MINECARTS({ it is AbstractMinecartEntity }, ::allEntities),
    ITEM_FRAMES({ it is ItemFrameEntity }, ::allEntities);

    override val belongsFunc: (Entity) -> Boolean = { this.genericBaseBelongsFunc(it) && this._belongsFunc(it) }

    override val provider = ResettableLazy {
        this.baseIterable()?.filter(this._belongsFunc)
    }
}

@Suppress("unused")
enum class BlockEntityCategory(
    override val belongsFunc: (BlockEntity) -> Boolean,
    internal val baseCollection: () -> List<BlockEntity>?
) : CategorisedTargetProvider<BlockEntity> {
    NONE({ false }, ::emptyList),
    ALL_BLOCK_ENTITIES({ true }, ::allBlockEntities),
    CONTAINERS({ it is Inventory }, ::allBlockEntities),
    CHESTS({ it is ChestBlockEntity }, ::allBlockEntities),
    ENDER_CHESTS({ it is EnderChestBlockEntity }, ::allBlockEntities),
    SHULKERS({ it is ShulkerBoxBlockEntity }, CONTAINERS::iterator);

    override val provider = ResettableLazy {
        this.baseCollection()?.filter(belongsFunc)
    }
}

@Suppress("unused")
enum class BlockCategory(override val belongsFunc: (Block) -> Boolean) : CategorisedTargetProvider<Block> {
    NONE({ false }),
    ORES({ it is OreBlock }),
    SLABS({ it is SlabBlock }),
    FERTILIZABLE({ it is Fertilizable }),
    WATERLOGGABLE({ it is Waterloggable });

    override val provider: ResettableLazy<List<Block>?> = ResettableLazy { emptyList() }
}

@Suppress("unused")
enum class ItemCategory(override val belongsFunc: (Item) -> Boolean) : CategorisedTargetProvider<Item> {
    // sorted by priority
    NONE({ false }),
    GRIEFING_TOOLS({ it in griefingTools }),
    AXES({ it is AxeItem }),
    SHOVELS({ it is ShovelItem }),
    PICKAXES({ it is PickaxeItem }),
    SWORDS({ it is SwordItem }),
    HOES({ it is HoeItem }),
    TOOLS({ it is ToolItem || it in additionalTools }),
    WEARABLE({ it is Wearable || (it as? BlockItem)?.block is Wearable }),
    FOOD({ it.isFood || it == Items.CAKE }),
    REDSTONE({ it.group == ItemGroup.REDSTONE || it in additionalRedstone }),
    BLOCKS({ it is BlockItem });

    companion object {
        private val griefingTools = arrayOf(
            Items.TNT,
            Items.TNT_MINECART,
            Items.FLINT,
            Items.FLINT_AND_STEEL,
            Items.GUNPOWDER,
            Items.SAND,
            Items.LAVA_BUCKET,
            Items.WATER_BUCKET,
            Items.END_CRYSTAL
        )

        private val additionalTools = arrayOf(
            Items.SHEARS,
            Items.FISHING_ROD,
            Items.FLINT_AND_STEEL
        )

        private val additionalRedstone = arrayOf(
            Items.SLIME_BLOCK,
            Items.HONEY_BLOCK,
            Items.REDSTONE_ORE, // this is here since redstone ore can detect updates to some extend and thus is usable in contraptions
            Items.ACTIVATOR_RAIL,
            Items.DETECTOR_RAIL,
            Items.POWERED_RAIL,
            Items.TNT_MINECART
        )
    }

    override val provider: ResettableLazy<List<Item>?> = ResettableLazy { emptyList() }
}

@Suppress("unused")
val invalidationListener = Listener<InGame>({
    EntityCategory.values().forEach(CategorisedTargetProvider<*>::invalidate)
    BlockEntityCategory.values().forEach(CategorisedTargetProvider<*>::invalidate)
}).also {
    KamiMod.EVENT_BUS.subscribe(it)
}