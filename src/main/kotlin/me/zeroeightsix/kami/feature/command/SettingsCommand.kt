package me.zeroeightsix.kami.feature.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import io.github.fablabsmc.fablabs.api.fiber.v1.FiberId
import io.github.fablabsmc.fablabs.api.fiber.v1.tree.ConfigBranch
import io.github.fablabsmc.fablabs.api.fiber.v1.tree.ConfigLeaf
import io.github.fablabsmc.fablabs.api.fiber.v1.tree.ConfigNode
import me.zeroeightsix.kami.feature.FullFeature
import me.zeroeightsix.kami.setting.getAnyInterface
import me.zeroeightsix.kami.setting.getAnyRuntimeConfigType
import me.zeroeightsix.kami.setting.settingInterface
import me.zeroeightsix.kami.setting.visibilityType
import me.zeroeightsix.kami.util.text
import net.minecraft.server.command.CommandSource
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import net.minecraft.util.Formatting.GOLD
import net.minecraft.util.Formatting.GRAY
import net.minecraft.util.Formatting.GREEN
import net.minecraft.util.Formatting.ITALIC
import net.minecraft.util.Formatting.LIGHT_PURPLE
import net.minecraft.util.Formatting.RED
import net.minecraft.util.Formatting.YELLOW
import java.util.function.Function
import java.util.stream.Stream

object SettingsCommand : Command() {
    private val FAILED_EXCEPTION =
        DynamicCommandExceptionType(
            Function { o: Any ->
                LiteralText(o.toString())
            }
        )

    override fun register(dispatcher: CommandDispatcher<CommandSource>) {
        val featureArgumentType = FeatureArgumentType.fullFeature()
        val settingArgumentType = SettingArgumentType.setting(featureArgumentType, "feature", 1)
        dispatcher.register(
            LiteralArgumentBuilder.literal<CommandSource>("settings")
                .then(
                    LiteralArgumentBuilder.literal<CommandSource>("list").then(
                        RequiredArgumentBuilder.argument<CommandSource, FullFeature>(
                            "feature",
                            FeatureArgumentType.fullFeature()
                        )
                            .executes { context: CommandContext<CommandSource> ->
                                val source = context.source as KamiCommandSource
                                val f =
                                    context.getArgument(
                                        "feature",
                                        FullFeature::class.java
                                    ) as FullFeature
                                source replyWith text(ITALIC) {
                                    +f.name(YELLOW)
                                    +" has the following properties:"(GOLD)
                                }
                                f.config.list().forEach {
                                    it?.let { source.sendFeedback(it) }
                                }
                                0
                            }
                    )
                )
                .then(
                    LiteralArgumentBuilder.literal<CommandSource>("set")
                        .then(
                            RequiredArgumentBuilder.argument<CommandSource, FullFeature>(
                                "feature",
                                featureArgumentType
                            )
                                .then(
                                    RequiredArgumentBuilder.argument<CommandSource, ConfigLeaf<Any>>(
                                        "setting",
                                        settingArgumentType as ArgumentType<ConfigLeaf<Any>>
                                    )
                                        .then(
                                            RequiredArgumentBuilder.argument<CommandSource, String>(
                                                "value",
                                                SettingValueArgumentType.value(
                                                    settingArgumentType as ArgumentType<ConfigLeaf<*>>,
                                                    "setting",
                                                    1
                                                )
                                            )
                                                .executes { context: CommandContext<CommandSource> ->
                                                    val feature =
                                                        context.getArgument(
                                                            "feature",
                                                            FullFeature::class.java
                                                        ) as FullFeature
                                                    val setting =
                                                        context.getArgument(
                                                            "setting",
                                                            ConfigLeaf::class.java
                                                        ) as ConfigLeaf<*>
                                                    val stringValue = context.getArgument(
                                                        "value",
                                                        String::class.java
                                                    ) as String
                                                    val configType = setting.getAnyRuntimeConfigType()
                                                    val interf = configType?.settingInterface
                                                    interf?.let {
                                                        val runtimeValue = interf.valueFromString(stringValue)
                                                        setting.value = configType.toSerializedType(runtimeValue)
                                                        val value = interf.valueToString(runtimeValue) ?: ""
                                                        context replyWith text(GOLD) {
                                                            +"Set property "
                                                            +setting.name(YELLOW)
                                                            +" of module "
                                                            +feature.name(YELLOW)
                                                            +" to "
                                                            +value(LIGHT_PURPLE)
                                                            +"!"
                                                        }
                                                    } ?: run {
                                                        context replyWith text(
                                                            RED,
                                                            "This setting can not be changed using the settings command."
                                                        )
                                                    }
                                                    0
                                                }
                                        )
                                )
                        )
                )
        )
    }
}

fun ConfigNode.list(): Stream<Text?> = when (this) {
    is ConfigBranch -> {
        this.items.stream().flatMap { it.list() }
    }
    is ConfigLeaf<*> -> {
        if (getAttributeValue(FiberId("kami", "setting_visibility"), visibilityType).map { it.isVisible() }
                .orElse(true)
        ) {
            Stream.of(this.list())
        } else {
            Stream.empty()
        }
    }
    else -> {
        Stream.of(text(null, "unknown node"))
    }
}

fun <T> ConfigLeaf<T>.list(): Text? {
    val interf = this.getAnyInterface() ?: return null
    val type = interf.type
    val value = interf.valueToString(getAnyRuntimeConfigType()?.toRuntimeType(this.value)) ?: ""
    return text {
        +name(YELLOW)
        +" ("(GRAY)
        +type(GREEN)
        +") "(GRAY)
        +"= "(GOLD)
        +value(LIGHT_PURPLE)
    }
}
