package me.zeroeightsix.kami.target

import imgui.ImGui
import imgui.ImGui.alignTextToFramePadding
import imgui.ImGui.columns
import imgui.ImGui.inputText
import imgui.ImGui.sameLine
import imgui.ImGui.separator
import imgui.ImGui.smallButton
import imgui.ImGui.textDisabled
import imgui.ImGui.treeNode
import imgui.ImGui.treeNodeEx
import imgui.ImGui.treePop
import imgui.flag.ImGuiCol
import imgui.flag.ImGuiTreeNodeFlags
import imgui.type.ImInt
import io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.RecordSerializableType
import io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.derived.ConfigType
import io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.derived.ConfigTypes
import io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.derived.RecordConfigType
import io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.derived.StringConfigType
import me.zero.alpine.event.EventPriority
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.event.TickEvent
import me.zeroeightsix.kami.gui.ImguiDSL.colors
import me.zeroeightsix.kami.gui.ImguiDSL.combo
import me.zeroeightsix.kami.gui.ImguiDSL.withId
import me.zeroeightsix.kami.gui.ImguiDSL.withStyleColour
import me.zeroeightsix.kami.gui.ImguiDSL.wrapImString
import me.zeroeightsix.kami.kotlin
import me.zeroeightsix.kami.mc
import me.zeroeightsix.kami.setting.GenerateType.Companion.columnMode
import me.zeroeightsix.kami.setting.InvalidValueException
import me.zeroeightsix.kami.setting.SettingInterface
import me.zeroeightsix.kami.setting.extend
import me.zeroeightsix.kami.setting.settingInterface
import me.zeroeightsix.kami.tempSet
import me.zeroeightsix.kami.tryOrNull
import me.zeroeightsix.kami.util.ResettableLazy
import net.minecraft.block.Block
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.item.Item
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import java.util.*
import java.util.stream.Collectors

val String.humanReadable
    get() = this.replace('_', ' ').toLowerCase().capitalize()

/**
 * This class implements the basic actions of a [TargetSupplier] type.
 */
abstract class TargetSupplier<T, M, E, S : TargetSupplier.SpecificTarget<T>>(
    val enumTargets: Map<E, M>,
    val specificTargets: Map<S, M>
) where E : Enum<E>, E : CategorisedTargetProvider<T> {
    init {
        Listener<TickEvent.InGame>({
            this.invalidate()
        }, EventPriority.HIGHEST + 1).also {
            KamiMod.EVENT_BUS.subscribe(it)
        }
    }

    private var targetsDelegate = ResettableLazy {
        flat()
    }

    val targets: MutableMap<T, M> by targetsDelegate

    /**
     * @return nonnull instance of [M] if `target` is supplied by this [TargetSupplier]
     */
    operator fun get(target: T) =
        this.enumTargets.entries.find { (e, _) -> e.belongsFunc(target) }?.value
            ?: this.specificTargets.entries.find { (e, _) -> e.belongs(target) }?.value

    private fun invalidate() {
        this.targetsDelegate.invalidate()
    }

    protected open fun flat(): MutableMap<T, M> {
        val map = mutableMapOf<T, M>()
        this.enumTargets.forEach { (target, meta) -> target.provider.value?.forEach { map[it] = meta } }
        this.specificTargets.forEach { (sTarget, meta) -> sTarget.targets?.forEach { map[it] = meta } }
        return map
    }

    abstract class SpecificTarget<T> {
        abstract val targets: Iterable<T>?
        abstract fun belongs(target: T): Boolean
        abstract fun imguiEdit()
    }
}

abstract class RegistrySpecificTarget<T, R>(
    _typeIdentifier: Identifier = noneIdentifier,
    private val registry: Registry<R>
) : TargetSupplier.SpecificTarget<T>() {
    companion object {
        internal val noneIdentifier = Identifier("kami", "none")
    }

    private val Identifier.pretty
        get() = if (this == noneIdentifier) "" else if (namespace == "minecraft") this.path else this.toString()

    var typeIdentifier = _typeIdentifier
        private set(value) {
            field = value
            this.registryEntry = fetchRegistryEntry()?.also {
                this.editStr = value.pretty
            }
        }

    private var editStr = typeIdentifier.pretty

    protected var registryEntry = fetchRegistryEntry()
        private set

    private fun fetchRegistryEntry() = typeIdentifier.let { if (it == noneIdentifier) null else this.registry[it] }

    override fun imguiEdit() {
        if (wrapImString(::editStr, { inputText("##${hashCode()}", it) })) {
            tryOrNull { Identifier(editStr) }?.let { id ->
                registry.getOrEmpty(id).kotlin?.let {
                    this.typeIdentifier = id
                }
            }
        }
    }

    override fun toString(): String = this.typeIdentifier.path.humanReadable
}

class EntitySupplier<M>(
    enumTargets: Map<EntityCategory, M>,
    specificTargets: Map<SpecificEntity, M>
) : TargetSupplier<Entity, M, EntityCategory, EntitySupplier.SpecificEntity>(
    enumTargets, specificTargets
) {
    class SpecificEntity(identifier: Identifier = noneIdentifier) :
        RegistrySpecificTarget<Entity, EntityType<*>>(identifier, Registry.ENTITY_TYPE) {
        override val targets
            get() = mc.world?.entities?.filter(::belongs)

        override fun belongs(target: Entity): Boolean = target.type == this.registryEntry
    }

    override fun flat(): MutableMap<Entity, M> {
        val map = super.flat()
        // Exclude the player from things like tracers
        mc.player?.let { map.remove(it) }
        return map
    }
}

class BlockEntitySupplier<M>(
    enumTargets: Map<BlockEntityCategory, M>,
    specificTargets: Map<SpecificBlockEntity, M>
) : TargetSupplier<BlockEntity, M, BlockEntityCategory, BlockEntitySupplier.SpecificBlockEntity>(
    enumTargets, specificTargets
) {
    class SpecificBlockEntity(identifier: Identifier = noneIdentifier) :
        RegistrySpecificTarget<BlockEntity, BlockEntityType<*>>(identifier, Registry.BLOCK_ENTITY_TYPE) {
        override val targets
            get() = mc.world?.blockEntities?.filter(::belongs)

        override fun belongs(target: BlockEntity): Boolean = target.type == this.registryEntry
    }
}

class BlockSupplier<M>(
    enumTargets: Map<BlockCategory, M>,
    specificTargets: Map<SpecificBlock, M>
) : TargetSupplier<Block, M, BlockCategory, BlockSupplier.SpecificBlock>(
    enumTargets, specificTargets
) {
    class SpecificBlock(identifier: Identifier = noneIdentifier) :
        RegistrySpecificTarget<Block, Block>(identifier, Registry.BLOCK) {
        override val targets = emptyList<Block>()

        override fun belongs(target: Block): Boolean = target == this.registryEntry
    }
}

class ItemSupplier<M>(
    enumTargets: Map<ItemCategory, M>,
    specificTargets: Map<ItemSupplier.SpecificItem, M>
) : TargetSupplier<Item, M, ItemCategory, ItemSupplier.SpecificItem>(enumTargets, specificTargets) {
    class SpecificItem(identifier: Identifier = noneIdentifier) :
        RegistrySpecificTarget<Item, Item>(identifier, Registry.ITEM) {
        override val targets = emptyList<Item>()

        override fun belongs(target: Item) = target == this.registryEntry
    }
}

inline fun <M, B, reified E : Enum<E>, reified S : TargetSupplier.SpecificTarget<*>, reified T : TargetSupplier<*, M, E, S>> createTargetsType(
    metaType: ConfigType<M, B, *>,
    enumType: StringConfigType<E>,
    specificTargetType: StringConfigType<S>,
    crossinline specFactory: () -> S,
    crossinline factory: (Map<E, M>, Map<S, M>) -> T
): RecordConfigType<T> {
    val catMapType = ConfigTypes.makeMap(enumType, metaType)
    val specMapType = ConfigTypes.makeMap(specificTargetType, metaType)
    val recordSerializableType = RecordSerializableType(
        mapOf(
            "categorised" to catMapType.serializedType,
            "specific" to specMapType.serializedType
        )
    )
    return RecordConfigType(
        recordSerializableType,
        T::class.java,
        {
            @Suppress("UNCHECKED_CAST") val categories: MutableMap<E, M> =
                catMapType.toRuntimeType(it["categorised"] as MutableMap<String, B>?)
            @Suppress("UNCHECKED_CAST") val specifics: MutableMap<S, M> =
                specMapType.toRuntimeType(it["specific"] as MutableMap<String, B>?)

            factory(categories, specifics)
        },
        {
            mapOf(
                "categorised" to catMapType.toSerializedType(it.enumTargets),
                "specific" to specMapType.toSerializedType(it.specificTargets)
            )
        }
    ).also { targetType ->
        val metaInterface = metaType.settingInterface

        val isColumns = metaInterface != null

        val next: () -> Unit = if (isColumns) {
            { ImGui.nextColumn() }
        } else {
            { }
        }

        targetType.extend(
            {
                "targets" // We don't try to convert targets <-> string
            },
            {
                throw InvalidValueException("Targets can not be set from the settings command.") // same here
            },
            { name, supplier ->
                // If either are nonnull, the factory is called & the target gets updated.
                var dirtyEnumMap: Map<E, M>? = null
                var dirtySpecMap: Map<S, M>? = null

                withStyleColour(ImGuiCol.Text, ImGui.getStyle().colors[ImGuiCol.TextDisabled]) {
                    val settingsOpen = treeNodeEx(name, ImGuiTreeNodeFlags.NoTreePushOnOpen)
                    if (!settingsOpen) return@extend null
                }

                withId(name) {
                    columns(if (isColumns) 2 else 1)
                    val specTargets = supplier.specificTargets
                    val enumTargets = supplier.enumTargets

                    // Whether or not there is a single target left. If true, the deletion button should not be shown.
                    val isSingleton = specTargets.size + enumTargets.size == 1

                    separator()

                    run {
                        var dirty = false

                        var i = 0
                        @Suppress("NAME_SHADOWING") val specMap = specTargets.mapNotNull { (spec, meta) ->
                            i++
                            val name = spec.toString()
                            var meta = meta

                            alignTextToFramePadding()

                            val nodeOpen = if (isColumns) {
                                // Show the node with the human readable name of the selected enum as title
                                treeNode("$name##$name-starget-$i", name)
                            } else false

                            next()

                            spec.imguiEdit()

                            if (!isSingleton && sameLine().run { smallButton("-") }) {
                                // Return null. `mapNotNull` will omit entries from the map that returned null.
                                dirty = true
                                return@mapNotNull null
                            }

                            next()

                            if (nodeOpen) {
                                editMeta(next, metaInterface, meta, dirty)?.let {
                                    dirty = true
                                    meta = it
                                }
                            }

                            spec to meta
                        }.toMap()

                        if (dirty)
                            dirtySpecMap = specMap
                    }

                    // All categories that have already been picked by the user
                    val chosenEnums = enumTargets.keys
                    // The categories that *haven't* been picked yet (set of enums \ chosen enums)
                    val availableEnums: Array<E> =
                        Arrays.stream(E::class.java.enumConstants).filter { !chosenEnums.contains(it) }
                            .collect(Collectors.toSet()).toTypedArray()

                    run {

                        var dirty = false

                        @Suppress("NAME_SHADOWING") val enumMap = enumTargets.mapNotNull { (enum, meta) ->
                            var enum = enum
                            var meta = meta

                            val humanReadableName = enum.name.humanReadable

                            withId(enum) {
                                alignTextToFramePadding()

                                val nodeOpen = if (isColumns) {
                                    // Show the node with the human readable name of the selected enum as title
                                    treeNode(humanReadableName)
                                } else false

                                next()

                                // We construct a list of available enums, but with the currently picked enum at idx 0 (otherwise it wouldn't be in the list of items to pick, so the user wouldn't have a item to click that 'cancels' the combobox)
                                val items = availableEnums.toMutableList().also {
                                    // Add the current enum at idx 0
                                    it.add(0, enum)
                                }.map { it.name.humanReadable } // Map them to human readable names

                                // Show the combobox for the user to select which enum to **switch** to
                                val currentItem = ImInt(0)
                                combo("##$name-etarget-$humanReadableName", currentItem, items) {
                                    // Get the index of the picked item. Minus 1 because we also inserted the (before this) picked item!
                                    val currentItem = currentItem.get() - 1
                                    // If selected combo item index was 0 (and thus, the element that we inserted), don't continue
                                    // (-1 because 0-1 = -1)
                                    if (currentItem != -1) {
                                        val currentItem = availableEnums[currentItem]
                                        enum = currentItem
                                        dirty = true
                                    }
                                }

                                // If there is more than one option, show a `-` button to delete a target.
                                // We don't want the user to be able to remove all targets as we'd have no meta to copy for a new target.
                                if (!isSingleton && sameLine().run { smallButton("-") }) {
                                    // Return null. `mapNotNull` will omit entries from the map that returned null.
                                    dirty = true
                                    return@mapNotNull null
                                }

                                next()

                                // If the tree node was opened, show this target's options
                                if (nodeOpen) {
                                    editMeta(next, metaInterface, meta, dirty)?.let {
                                        dirty = true
                                        meta = it
                                    }
                                }
                            }

                            enum to meta
                        }.toMap()

                        if (dirty)
                            dirtyEnumMap = enumMap
                    }

                    // Display the widget to add targets
                    // If columns, we display the label of the combobox separately.
                    if (isColumns) {
                        textDisabled("New")
                        next()
                    }

                    val currentItem = ImInt(-1)
                    val options = availableEnums
                        .map { it.name.humanReadable }
                        .toMutableList()
                        .also {
                            it.add(0, "Custom")
                        }

                    combo((if (isColumns) "##" else "") + "New##", currentItem, options) {
                        @Suppress("NAME_SHADOWING") val currentItem = currentItem.get()
                        // User selected 'Custom' option
                        if (currentItem == 0) {
                            // Add a new spec with the last spec meta as meta or the last enum meta as fallback meta
                            dirtySpecMap = supplier.specificTargets.toMutableMap().also {
                                it[specFactory()] = specTargets.values.lastOrNull() ?: enumTargets.values.last()
                            }
                        } else {
                            // User selected one of the categories (enums)
                            @Suppress("NAME_SHADOWING") val currentItem =
                                availableEnums[currentItem - 1] // -1 as offset for the added Custom option
                            dirtyEnumMap = supplier.enumTargets.toMutableMap().also {
                                // Add the selected enum, with meta from the last entry in the enum map.
                                it[currentItem] = enumTargets.values.lastOrNull()
                                    ?: specTargets.values.last() // Copy the meta from the last entry
                            }
                        }
                    }
                }
                separator()


                if (dirtyEnumMap != null || dirtySpecMap != null) {
                    factory(dirtyEnumMap ?: supplier.enumTargets, dirtySpecMap ?: supplier.specificTargets)
                } else null
            }
        )
    }
}

@Suppress("NAME_SHADOWING")
fun <M> editMeta(
    next: () -> Unit,
    metaInterface: SettingInterface<M>?,
    meta: M,
    dirty: Boolean
): M? {
    var meta = meta
    var dirty = dirty
    // For types generated by @GenerateType, turn on column mode.
    // Most target meta types are generated this way.
    ::columnMode.tempSet(true) {
        next()
        metaInterface?.let {
            it.displayImGui(it.type.capitalize(), meta)?.let { changedMeta ->
                dirty = true
                meta = changedMeta
            }
        }
        next()
    }
    treePop()

    return if (dirty) meta else null
}