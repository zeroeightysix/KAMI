package me.zeroeightsix.kami.setting

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import imgui.ImGui
import imgui.MutableProperty0
import io.github.fablabsmc.fablabs.api.fiber.v1.FiberId
import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting
import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.processor.LeafAnnotationProcessor
import io.github.fablabsmc.fablabs.api.fiber.v1.builder.ConfigLeafBuilder
import io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.derived.ConfigTypes
import io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.derived.StringConfigType
import io.github.fablabsmc.fablabs.api.fiber.v1.tree.ConfigAttribute
import io.github.fablabsmc.fablabs.api.fiber.v1.tree.ConfigLeaf
import net.minecraft.server.command.CommandSource
import net.minecraft.util.Identifier
import java.lang.reflect.Field
import java.util.concurrent.CompletableFuture

object SettingAnnotationProcessor : LeafAnnotationProcessor<Setting> {

    private val typeMap = mutableMapOf<Class<*>, SettingInterface<*>>(
        Pair(
            Boolean::class.java,
            object : SettingInterface<Boolean> {
                override val id: Identifier = Identifier("kami:boolean_displayer")

                override fun displayTypeAndValue(leaf: ConfigLeaf<Boolean>): Pair<String, String> =
                    Pair("boolean", leaf.value.toString())

                override fun valueFromString(str: String): Boolean
                    = str.toBoolean()

                override fun displayImGui(leaf: ConfigLeaf<Boolean>) {
                    with (ImGui) {
                        checkbox(leaf.name, leaf.asMutableProperty())
                    }
                }

                override fun listSuggestions(
                    context: CommandContext<*>,
                    builder: SuggestionsBuilder
                ): CompletableFuture<Suggestions> =
                    CommandSource.suggestMatching(listOf("true", "false"), builder)
            }
        )
    )

    init {
        typeMap.values.forEach {
            SettingInterface.Registry.add(it.id, it)
        }
    }

    fun <T> ConfigLeaf<T>.asMutableProperty() = object : MutableProperty0<T>(this.value) {
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
                typeMap.getOrDefault(field!!.type, SettingInterface.Default)
            )
        )
    }

}