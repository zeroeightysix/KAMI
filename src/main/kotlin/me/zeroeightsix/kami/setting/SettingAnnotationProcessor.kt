package me.zeroeightsix.kami.setting

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
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
import me.zeroeightsix.kami.MutableProperty
import me.zeroeightsix.kami.setting.SettingAnnotationProcessor.asMutableProperty
import net.minecraft.server.command.CommandSource
import net.minecraft.util.Identifier
import java.lang.reflect.Field
import java.math.BigDecimal
import java.util.concurrent.CompletableFuture

object SettingAnnotationProcessor : LeafAnnotationProcessor<Setting> {

    private val typeMap = mutableMapOf<Class<*>, SettingInterface<*>>(
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
            }, listOf("true", "false"))
        ),
        Pair(
            BigDecimal::class.java,
            createInterface({
                Pair("number", it.value.toString())
            }, {
                it.toBigDecimal()
            }, {
                with (ImGui) {
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
            }, listOf("1", "10", "50"))
        )
    )

    /**
     * Method to create an interface object by using lambda functions to reduce boilerplate
     */
    private inline fun <reified T> createInterface(
        crossinline typeAndValue: (ConfigLeaf<T>) -> Pair<String, String>,
        crossinline fromString: (String) -> T,
        crossinline imGui: (ConfigLeaf<T>) -> Unit,
        suggestions: List<String>,
        name: String = T::class.java.simpleName.toLowerCase()
    ) = object : SettingInterface<T> {
        override val id = Identifier("kami", "${name}_interface")

        override fun displayTypeAndValue(leaf: ConfigLeaf<T>): Pair<String, String> = typeAndValue(leaf)
        override fun valueFromString(str: String): T = fromString(str)
        override fun displayImGui(leaf: ConfigLeaf<T>) = imGui(leaf)

        override fun listSuggestions(
            context: CommandContext<*>,
            builder: SuggestionsBuilder
        ): CompletableFuture<Suggestions> = CommandSource.suggestMatching(suggestions, builder)
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
                typeMap.getOrDefault(field!!.type, typeMap.getOrDefault(builder.type.erasedPlatformType, SettingInterface.Default))
            )
        )
    }

}