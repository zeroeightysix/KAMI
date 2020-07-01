package me.zeroeightsix.kami.setting

import com.google.common.collect.Streams
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import glm_.vec2.Vec2
import imgui.ImGui
import io.github.fablabsmc.fablabs.api.fiber.v1.FiberId
import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting
import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.processor.LeafAnnotationProcessor
import io.github.fablabsmc.fablabs.api.fiber.v1.builder.ConfigLeafBuilder
import io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.DecimalSerializableType
import io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.derived.ConfigTypes
import io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.derived.StringConfigType
import io.github.fablabsmc.fablabs.api.fiber.v1.tree.ConfigAttribute
import io.github.fablabsmc.fablabs.api.fiber.v1.tree.ConfigLeaf
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.MutableProperty
import me.zeroeightsix.kami.mixin.extend.getMap
import me.zeroeightsix.kami.util.Bind
import net.minecraft.client.util.InputUtil
import net.minecraft.server.command.CommandSource
import net.minecraft.util.Identifier
import java.lang.reflect.Field
import java.math.BigDecimal
import java.util.concurrent.CompletableFuture
import java.util.stream.Stream

object SettingAnnotationProcessor : LeafAnnotationProcessor<Setting> {

    private val bindMap = (InputUtil.Type.KEYSYM.getMap()).mapNotNull {
        val name = it.value.name.also { s ->
            if (!s.startsWith("key.keyboard")) return@mapNotNull null
        }.removePrefix("key.keyboard.").replace('.', '-')

        Pair(name, it.value!!)
    }.toMap()

    private val typeMap = mutableMapOf<Class<*>, SettingInterface<*>>(
        Pair(
            Bind::class.java,
            createInterface({
                Pair("bind", KamiMod.bindType.toRuntimeType(it.value).toString())
            }, {
                var s = it.toLowerCase()

                fun remove(part: String): Boolean {
                    val removed = s.replace(part, "")
                    val changed = s == removed
                    s = removed
                    return changed
                }

                val ctrl = remove("ctrl+")
                val alt = remove("alt+")
                val shift = remove("shift+")

                KamiMod.bindType.toSerializedType(Bind(
                    ctrl,
                    alt,
                    shift,
                    bindMap[s]
                ))
            }, {
                with(ImGui) {
                    val bind = KamiMod.bindType.toRuntimeType(it.value)
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

    /**
     * Method to create an interface object by using lambda functions to reduce boilerplate
     */
    private inline fun <reified T> createInterface(
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

    init {
        typeMap.values.forEach {
            SettingInterface.Registry.add(it.id, it)
        }
    }

    fun <T> ConfigLeaf<T>.asMutableProperty() = object : MutableProperty<T>(this.value) {
        override fun set(value: T) {
            this@asMutableProperty.value = value
        }
    }

    val INTERFACE_TYPE: StringConfigType<SettingInterface<*>> =
        ConfigTypes.STRING.derive(SettingInterface::class.java,
            {
                SettingInterface.Registry[Identifier(it)]
            },
            {
                it!!.id.toString()
            })

    override fun apply(annotation: Setting?, field: Field?, pojo: Any?, builder: ConfigLeafBuilder<*, *>?) {
        builder!!.withAttribute(
            ConfigAttribute.create(
                FiberId("kami", "setting_interface"),
                INTERFACE_TYPE,
                typeMap.getOrDefault(
                    field!!.type,
                    typeMap.getOrDefault(builder.type.erasedPlatformType, SettingInterface.Default)
                )
            )
        )
    }

}