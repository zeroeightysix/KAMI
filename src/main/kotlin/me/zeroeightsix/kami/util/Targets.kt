package me.zeroeightsix.kami.util

import imgui.ImGui
import imgui.dsl
import io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.derived.ConfigType
import io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.derived.ConfigTypes
import io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.derived.StringConfigType
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.*
import me.zeroeightsix.kami.event.TickEvent
import me.zeroeightsix.kami.setting.InvalidValueException
import me.zeroeightsix.kami.setting.extend
import me.zeroeightsix.kami.setting.settingInterface
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.ChestBlockEntity
import net.minecraft.block.entity.ShulkerBoxBlockEntity
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.mob.AmbientEntity
import net.minecraft.entity.mob.Angerable
import net.minecraft.entity.mob.Monster
import net.minecraft.entity.mob.WaterCreatureEntity
import net.minecraft.entity.passive.IronGolemEntity
import net.minecraft.entity.passive.PassiveEntity
import net.minecraft.entity.passive.WolfEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventory
import kotlin.collections.map

val invalidationListener = Listener<TickEvent.Client.InGame>({
    EntityTarget.values().forEach { it.invalidate() }
}).also {
    KamiMod.EVENT_BUS.subscribe(it)
}

fun isPassive(e: Entity): Boolean {
    if (e is Monster || (e is WolfEntity && e.angryAt != mc.player?.uuid)) return false
    return if (e is PassiveEntity || e is AmbientEntity || e is WaterCreatureEntity) true else e is IronGolemEntity && e.target === null
}

fun isHostile(e: Entity) = e is Monster || (e is Angerable && e.angryAt == mc.player?.uuid)

operator fun <T> ((T) -> Boolean).not() = { t: T -> !this(t) }

private fun allEntities() = mc.world?.entities
private fun allPlayers() = mc.world?.players

enum class EntityTarget(
    val belongsFunc: (Entity) -> Boolean,
    internal val baseCollection: () -> Iterable<Entity>?,
    /**
     * Player targets work on the pre-filtered set of players minecraft provides.
     * That means their `belongsFunc` will produce false positives when tested against non-player entities.
     * This method is used to identify whether or not an entity belongs to the base collection the target uses.
     */
    internal val genericBaseBelongsFunc: (Entity) -> Boolean = { true }
) {
    NONE({ false }, ::emptyList, { false }),
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

abstract class Targets<T : Enum<T>, M, E>(private val inner: Map<T, M>) : Map<T, M> by inner {
    val entities: MutableMap<E, M>
        get() = flat()

    abstract fun flat(): MutableMap<E, M>
}

private fun allBlockEntities() = mc.world?.blockEntities

enum class BlockTarget(
    val belongsFunc: (BlockEntity) -> Boolean,
    internal val baseCollection: () -> Iterable<BlockEntity>?
) {
    NONE({ false }, ::emptyList),
    ALL_BLOCK_ENTITIES({ true }, ::allBlockEntities),
    CONTAINERS({ it is Inventory }, ::allBlockEntities),
    CHESTS({ it is ChestBlockEntity }, ::allBlockEntities),
    SHULKERS({ it is ShulkerBoxBlockEntity }, ::allBlockEntities);

    val provider = ResettableLazy {
        this.baseCollection()?.filter { this.belongsFunc(it) }
    }
    val entities by provider

    fun invalidate() = provider.invalidate()
}

class EntityTargets<T>(inner: Map<EntityTarget, T>) : Targets<EntityTarget, T, Entity>(inner) {
    /**
     * Produces a flatmap of this [EntityTargets] underlying targeted entities.
     *
     * It is ensured to have no duplicates, where the meta of the last target in the map takes priority if a duplicate occurs.
     */
    override fun flat(): MutableMap<Entity, T> {
        val map = mutableMapOf<Entity, T>()
        for ((target, t) in this) {
            target.entities?.forEach { map[it] = t }
        }
        map.remove(mc.player) // We never want the player included in the entity list. Sorry bud.
        return map
    }

    /**
     * @return `true` if `entity` belongs to this [EntityTargets]
     */
    fun belongs(entity: Entity) =
        this.entries.find { it.key.genericBaseBelongsFunc(entity) && it.key.belongsFunc(entity) }?.value
}

class BlockTargets<T>(inner: Map<BlockTarget, T>) : Targets<BlockTarget, T, BlockEntity>(inner) {
    /**
     * Produces a flatmap of this [EntityTargets] underlying targeted entities.
     *
     * It is ensured to have no duplicates, where the meta of the last target in the map takes priority if a duplicate occurs.
     */
    override fun flat(): MutableMap<BlockEntity, T> {
        val map = mutableMapOf<BlockEntity, T>()
        for ((target, t) in this) {
            target.entities?.forEach { map[it] = t }
        }
        return map
    }

    /**
     * @return `true` if `entity` belongs to this [EntityTargets]
     */
    fun belongs(blockEntity: BlockEntity) = this.entries.find { it.key.belongsFunc(blockEntity) }?.value
}

inline fun <M, S, reified T : Enum<T>, reified C : Targets<T, M, *>> createTargetsType(
    metaType: ConfigType<M, S, *>,
    targetConfigType: StringConfigType<T>,
    crossinline factory: (Map<T, M>) -> C
) =
    ConfigTypes.makeMap(targetConfigType, metaType).derive(C::class.java, {
        factory(it)
    }, {
        it
    }).also {
        metaType.settingInterface?.let { interf ->
            val metaName = interf.type.capitalize()
            it.extend({
                "targets" // We don't try to convert targets <-> string
            }, {
                throw InvalidValueException("Targets can not be set from the settings command.") // same here
            }, { name, value ->
                val possibleTargets =
                    T::class.java.enumConstants.map { it.name.humanReadable() to it }.toMap().toMutableMap()
                var index = 0
                var modified: C? = null

                with(ImGui) {
                    dsl.columns("$name-targets-columns", 2) {
                        text("%s", name)
                        nextColumn()
                        text("%s", metaName)
                        separator()
                        nextColumn()

                        var dirty = false

                        val map = value.mapNotNull { (target, meta) ->
                            // The target to return. If null, remove this entry.
                            var retT: T? = target
                            // The meta to return
                            var retM: M = meta

                            val strings = possibleTargets.keys.toList()
                            val targetReadable = target.name.humanReadable()
                            val array = intArrayOf(strings.indexOf(targetReadable))
                            combo("##$name-target-$index", array, strings.toList()).then {
                                possibleTargets[strings[array[0]]]?.let { retT = it }
                            }

                            // Users are not allowed to remove the last remaining target, as it is required for copying over the meta when creating new targets.
                            if (value.size > 1) {
                                sameLine()
                                button("-##$name-target-$index-rm").then {
                                    retT = null // Return nothing, which removes the entry from the map.
                                }
                            }
                            index++

                            // To avoid duplicate entries (which aren't possible, so the UI would act weird when you try to make one)
                            possibleTargets.remove(targetReadable)

                            nextColumn()
                            interf.displayImGui("$metaName##$name-target-$metaName-$index", meta)?.let {
                                retM = it
                                // the meta object itself might actually be mutated instead of being a new object.
                                // thus, the map == value check might say that they're the same because the same object in the original map was also changed.
                                // therefore we set this flag to make sure the 'new' map is processed.
                                dirty = true
                            }
                            nextColumn()

                            retT?.let {
                                it to retM
                            }
                        }.toMap().toMutableMap()

                        if (possibleTargets.isNotEmpty()) {
                            separator()
                            val strings = possibleTargets.keys.toList()
                            val array = intArrayOf(-1)
                            combo("New##$name-target-new", array, strings).then {
                                // I can't be bothered to implement a default meta constant, so we just copy over the last meta type as the value for this new entry
                                // This does require there to always be a target entry, though
                                // please don't make empty targets, will you?
                                possibleTargets[strings[array[0]]]?.let { map[it] = value.values.last() }
                            }
                        }

                        if (dirty || map != value) {
                            modified = factory(map)
                        }
                    }
                }

                modified
            })
        }
    }
