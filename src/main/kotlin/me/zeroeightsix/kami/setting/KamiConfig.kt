package me.zeroeightsix.kami.setting

import com.mojang.authlib.GameProfile
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import glm_.asHexString
import glm_.vec2.Vec2
import imgui.ColorEditFlag
import imgui.ImGui
import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.AnnotatedSettings
import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting
import io.github.fablabsmc.fablabs.api.fiber.v1.builder.ConfigTreeBuilder
import io.github.fablabsmc.fablabs.api.fiber.v1.exception.ValueDeserializationException
import io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.DecimalSerializableType
import io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.derived.ConfigTypes
import io.github.fablabsmc.fablabs.api.fiber.v1.serialization.FiberSerialization
import io.github.fablabsmc.fablabs.api.fiber.v1.serialization.JanksonValueSerializer
import io.github.fablabsmc.fablabs.api.fiber.v1.tree.ConfigLeaf
import io.github.fablabsmc.fablabs.api.fiber.v1.tree.ConfigTree
import io.github.fablabsmc.fablabs.api.fiber.v1.tree.PropertyMirror
import me.zeroeightsix.kami.Colour
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.MutableProperty
import me.zeroeightsix.kami.feature.FeatureManager
import me.zeroeightsix.kami.feature.FeatureManager.fullFeatures
import me.zeroeightsix.kami.feature.FindSettings
import me.zeroeightsix.kami.feature.FullFeature
import me.zeroeightsix.kami.feature.module.Module
import me.zeroeightsix.kami.gui.windows.modules.Modules
import me.zeroeightsix.kami.mixin.extend.getMap
import me.zeroeightsix.kami.splitFirst
import me.zeroeightsix.kami.util.Bind
import me.zeroeightsix.kami.util.Friends
import net.minecraft.client.util.InputUtil
import net.minecraft.server.command.CommandSource
import net.minecraft.util.Identifier
import org.reflections.Reflections
import java.io.IOException
import java.math.BigDecimal
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer
import java.util.stream.Collectors
import java.util.stream.Stream
import kotlin.collections.HashMap

object KamiConfig {

    const val CONFIG_FILENAME = "KAMI_config.json5"

    val moduleType = ConfigTypes.STRING.derive<Module>(Module::class.java, { name ->
        FeatureManager.modules.find { it.name == name }
    }, { m ->
        m.name
    })
    val moduleListType = ConfigTypes.makeList(moduleType)
    val modulesGroupsType = ConfigTypes.makeMap(ConfigTypes.STRING, moduleListType)
    val windowsType = ConfigTypes.makeMap(ConfigTypes.STRING, modulesGroupsType)
        .derive(Modules.Windows::class.java, {
            val windows = mutableListOf<Modules.ModuleWindow>()
            for ((nameAndId, groups) in it) {
                val (id, name) = nameAndId.splitFirst('-')
                windows.add(
                    Modules.ModuleWindow(
                        name,
                        groups = groups.mapValues { it.value.toMutableList() }.toMutableMap(),
                        id = id.toInt()
                    )
                )
            }
            Modules.Windows(windows)
        }, {
            val map = mutableMapOf<String, Map<String, List<Module>>>()
            for (window in it) {
                val nameAndId = "${window.id}-${window.title}"
                map[nameAndId] = window.groups.toMutableMap()
            }
            map
        })

    val colourType =
        ConfigTypes.makeList(ConfigTypes.FLOAT)
            .derive(Colour::class.java, { list ->
                Colour(list[0], list[1], list[2], list[3])
            }, {
                listOf(it.r, it.g, it.b, it.a)
            })

    var bindType = ConfigTypes.STRING.derive(Bind::class.java, {
        var s = it.toLowerCase()

        fun remove(part: String): Boolean {
            val removed = s.replace(part, "")
            val changed = s != removed
            s = removed
            return changed
        }

        val ctrl = remove("ctrl+")
        val alt = remove("alt+")
        val shift = remove("shift+")

        s = s.removePrefix("key.keyboard.")

        Bind(
            ctrl,
            alt,
            shift,
            bindMap[s] ?: Bind.Code.none()
        )
    }, {
        var s = ""
        if (it.isCtrl) s += "ctrl+"
        if (it.isAlt) s += "alt+"
        if (it.isShift) s += "shift+"
        s += it.code.translationKey
        s
    })

    val profileType =
        ConfigTypes.makeMap(ConfigTypes.STRING, ConfigTypes.STRING)
            .derive(
                GameProfile::class.java,
                { map: Map<String?, String?> ->
                    GameProfile(
                        UUID.fromString(map["uuid"]),
                        map["name"]
                    )
                }
            ) { profile: GameProfile ->
                val map = HashMap<String?, String?>()
                map["uuid"] = profile.id.toString()
                map["name"] = profile.name
                map
            }
    val friendsType =
        ConfigTypes.makeList(profileType)

    private val bindMap = (InputUtil.Type.KEYSYM.getMap()).mapNotNull {
        val name = it.value.translationKey.also { s ->
            if (!s.startsWith("key.keyboard")) return@mapNotNull null
        }.removePrefix("key.keyboard.").replace('.', '-')

        Pair(name, Bind.Code(it.value!!))
    }.toMap()

    val typeMap = mutableMapOf<Class<*>, SettingInterface<*>>(
        Pair(
            Colour::class.java,
            createInterface({
                Pair("colour", colourType.toRuntimeType(it.value).asRGBA().asHexString)
            }, {
                colourType.toSerializedType(
                    Colour.fromRGBA(it.toInt(radix = 16))
                )
            }, {
                with(ImGui) {
                    val floats = colourType.toRuntimeType(it.value).asFloats().toFloatArray()
                    colorEdit4(
                        it.name,
                        floats,
                        ColorEditFlag.AlphaBar.i
                    )
                    it.value = colourType.toSerializedType(
                        Colour(floats[0], floats[1], floats[2], floats[3])
                    )
                }
            }, { b -> b.buildFuture() })
        ),
        Pair(
            Bind::class.java,
            createInterface({
                Pair("bind", bindType.toRuntimeType(it.value).toString())
            }, {
                it
            }, {
                with(ImGui) {
                    val bind = bindType.toRuntimeType(it.value)
                    text("Bound to $bind") // TODO: Highlight bind in another color?
                    sameLine(0, -1)
                    if (button("Bind", Vec2())) { // TODO: Bind popup?
                        // Maybe just display "Press a key" instead of the normal "Bound to ...", and wait for a key press.
                    }
                }
            }, { b ->
                val range = 0..b.remaining.lastIndexOf('+')
                val left = b.remaining.substring(range)
                val right = b.remaining.toLowerCase().removeRange(range)

                Stream.concat(
                    listOf("ctrl+", "shift+", "alt+").stream(),
                    bindMap.keys.stream()
                ).forEach {
                    if (it.startsWith(right)) b.suggest("$left$it")
                }

                b.buildFuture()
            })
        ),
        Pair(
            Boolean::class.java,
            createInterface({
                Pair("boolean", it.value.toString())
            }, {
                it.toBoolean()
            }, {
                with(ImGui) {
                    checkbox(it.name, it.asMutableProperty())
                }
            }, { b -> CommandSource.suggestMatching(listOf("true", "false"), b) })
        ),
        Pair(
            BigDecimal::class.java,
            createInterface({
                Pair("number", it.value.toString())
            }, {
                it.toBigDecimal()
            }, {
                with(ImGui) {
                    val configType = it.configType as DecimalSerializableType
                    val increment = configType.increment
                    dragFloat(
                        it.name,
                        object : MutableProperty<Float>(it.value.toFloat()) {
                            override fun set(value: Float?) {
                                it.value = BigDecimal.valueOf(value!!.toDouble())
                            }
                        },
                        vSpeed = increment?.toFloat() ?: 1.0f,
                        vMin = configType.minimum?.toFloat() ?: 0.0f,
                        vMax = configType.maximum?.toFloat() ?: 0.0f,
                        format = "%s"
                    )
                }
            }, { b -> b.buildFuture() })
        ),
        Pair(
            String::class.java,
            createInterface({
                Pair("text", it.value)
            }, {
                it
            }, {
                with(ImGui) {
                    inputText(it.name, it.value)
                }
            }, { b -> b.buildFuture() })
        )
    )

    val config = initAndLoad()

    /**
     * Method to create an interface object by using lambda functions to reduce boilerplate
     */
    inline fun <reified T> createInterface(
        crossinline typeAndValue: (ConfigLeaf<T>) -> Pair<String, String>,
        crossinline fromString: (String) -> T,
        crossinline imGui: (ConfigLeaf<T>) -> Unit,
        crossinline suggestions: (SuggestionsBuilder) -> CompletableFuture<Suggestions>,
        name: String = T::class.java.simpleName.toLowerCase()
    ) = object : SettingInterface<T> {
        override val id = Identifier("kami", "${name}_interface")

        override fun displayTypeAndValue(leaf: ConfigLeaf<T>): Pair<String, String> = typeAndValue(leaf)
        override fun valueFromString(str: String): T = fromString(str)
        override fun displayImGui(leaf: ConfigLeaf<T>) = imGui(leaf)
        override fun listSuggestions(
            context: CommandContext<*>,
            builder: SuggestionsBuilder
        ): CompletableFuture<Suggestions> = suggestions(builder)
    }

    fun <T> ConfigLeaf<T>.asMutableProperty() = object : MutableProperty<T>(this.value) {
        override fun set(value: T) {
            this@asMutableProperty.value = value
        }
    }

    fun initAndLoad(): ConfigTree? {
        typeMap.values.forEach {
            SettingInterface.interfaces[it.id] = it
        }

        return try {
            val config = constructConfiguration()
            try {
                loadConfiguration(config)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            KamiMod.log.info("Settings loaded")
            config
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Resolves all features annotated by [FindSettings] or affected by the annotation on a superclass.
     *
     * @return All found roots, mapped to a set of affected classes
     * @see FeatureManager.findAnnotatedFeatures
     */
    fun findAnnotatedSettings(): Map<String, List<Class<*>>> {
        val reflections = Reflections("me.zeroeightsix.kami")
        return reflections.getTypesAnnotatedWith(FindSettings::class.java).filter {
            it.isAnnotationPresent(FindSettings::class.java)
        }.map {
            val fsAnnot = it.getAnnotation(FindSettings::class.java)!!
            fsAnnot to if (fsAnnot.findDescendants) {
                reflections.getSubTypesOf(it)
            } else {
                Collections.singleton(it)
            }
        }.groupBy {
            it.first.settingsRoot
        }.mapValues {
            it.value.stream().flatMap { it.second.stream() }.collect(Collectors.toList())
        }
    }

    private fun constructConfiguration(): ConfigTree {
        val settings = AnnotatedSettings.builder()
            .collectMembersRecursively()
            .collectOnlyAnnotatedMembers()
            .useNamingConvention(ProperCaseConvention)
            .registerTypeMapping(Bind::class.java, bindType)
            .registerTypeMapping(GameProfile::class.java, profileType)
            .registerTypeMapping(Colour::class.java, colourType)
            .registerTypeMapping(Modules.Windows::class.java, windowsType)
            .registerSettingProcessor(
                Setting::class.java,
                SettingAnnotationProcessor
            )
            .registerSettingProcessor(
                SettingVisibility.Constant::class.java,
                ConstantVisibilityAnnotationProcessor
            )
            .registerSettingProcessor(
                SettingVisibility.Method::class.java,
                MethodVisibilityAnnotationProcessor
            )
            .build()

        val builder = ConfigTree.builder()

        // TODO: Eventually, when all modules are kotlin objects, we can reasonably assume that they'll have an INSTANCE field.
        // Then we can just add @FindSettings to the module class and remove this method
        constructFeaturesConfiguration(builder, settings)

        findAnnotatedSettings().also { println(it) }.entries.forEach { (root, list) ->
            val (builder, block) = when {
                root.isEmpty() -> Pair(builder, { _ -> Unit })
                else -> Pair(builder.fork(root), { builder: ConfigTreeBuilder -> builder.finishBranch(); Unit })
            }

            list.forEach {
                try {
                    val instance = it.getDeclaredField("INSTANCE").get(null)
                    val config = builder.applyFromPojo(instance, settings)
                    if (root.isNotEmpty()) {
                        val config = config.build()
                        if (instance is FullFeature) { // TODO: Maybe a `HasConfig` interface instead of relying on FullFeature
                            instance.config = config
                        }
                    }
                } catch (e: NoSuchFieldError) {
                    println("Couldn't get ${it.simpleName}'s instance, probably not a kotlin object!");
                    e.printStackTrace()
                }
            }

            block(builder)
        }

        val built = builder.build()

        val mirror =
            PropertyMirror.create(friendsType)
        mirror.mirror(
            built.lookupLeaf(
                "Friends",
                friendsType.serializedType
            )
        )
        Friends.mirror = mirror

        return built
    }

    private fun constructFeaturesConfiguration(
        builder: ConfigTreeBuilder,
        settings: AnnotatedSettings?
    ) {
        val features = builder.fork("features")
        // only full features because they have names
        fullFeatures.forEach(Consumer { f: FullFeature ->
            f.config = features.fork(f.name)
                .applyFromPojo(f, settings)
                .build()
        })
        features.finishBranch()
    }

    fun loadConfiguration() = loadConfiguration(this.config)

    @Throws(IOException::class, ValueDeserializationException::class)
    private fun loadConfiguration(config: ConfigTree?) {
        config?.let {
            try {
                FiberSerialization.deserialize(
                    config,
                    Files.newInputStream(Paths.get(CONFIG_FILENAME), StandardOpenOption.CREATE_NEW, StandardOpenOption.READ),
                    JanksonValueSerializer(false)
                )
            } catch (e: java.nio.file.NoSuchFileException) {
                saveConfiguration(config)
                KamiMod.log.info("KAMI configuration file generated!")
            } catch (e: Exception) {
                KamiMod.log.error("Failed to load KAMI configuration file", e)
            }
        }
    }

    @JvmStatic
    @Throws(IOException::class)
    fun saveConfiguration() = saveConfiguration(this.config)

    @Throws(IOException::class)
    private fun saveConfiguration(config: ConfigTree?) {
        config?.let {
            FiberSerialization.serialize(
                it,
                Files.newOutputStream(Paths.get(CONFIG_FILENAME)),
                JanksonValueSerializer(false)
            )
        }
    }

}
