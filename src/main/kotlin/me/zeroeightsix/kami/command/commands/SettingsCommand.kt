package me.zeroeightsix.kami.command.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import me.zeroeightsix.kami.command.*
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.util.Texts
import net.minecraft.server.command.CommandSource
import net.minecraft.text.LiteralText
import net.minecraft.util.Formatting
import java.util.function.Consumer
import java.util.function.Function

/**
 * Created by 086 on 18/11/2017.
 */
object SettingsCommand : Command() {
    private val FAILED_EXCEPTION =
        DynamicCommandExceptionType(Function { o: Any ->
            LiteralText(o.toString())
        })

    override fun register(dispatcher: CommandDispatcher<CommandSource>) {
        val moduleArgumentType = ModuleArgumentType.module()
        val settingArgumentType = SettingArgumentType.setting(moduleArgumentType, "module", 1)
        dispatcher.register(
            LiteralArgumentBuilder.literal<CommandSource>("settings")
                .then(
                    LiteralArgumentBuilder.literal<CommandSource>("list").then(
                        RequiredArgumentBuilder.argument<CommandSource, Module>(
                            "module",
                            ModuleArgumentType.module()
                        )
                            .executes { context: CommandContext<CommandSource> ->
                                val source = context.source as KamiCommandSource
                                val m =
                                    context.getArgument(
                                        "module",
                                        Module::class.java
                                    ) as Module
                                source.sendFeedback(
                                    Texts.i(
                                        Texts.append(
                                            Texts.flit(
                                                Formatting.YELLOW,
                                                m.name.value
                                            ),
                                            Texts.flit(
                                                Formatting.GOLD,
                                                " has the following properties:"
                                            )
                                        )
                                    )
                                )
                                m.settingList.forEach(
                                    Consumer { setting: Setting<*> ->
                                        val settingName = setting.name
                                        val typeName = setting.valueClass.simpleName
                                        val value = setting.valueAsString
                                        source.sendFeedback(
                                            Texts.append(
                                                Texts.flit(
                                                    Formatting.YELLOW,
                                                    settingName
                                                ),
                                                Texts.lit(" "),
                                                Texts.flit(
                                                    Formatting.GRAY,
                                                    "("
                                                ),
                                                Texts.flit(
                                                    Formatting.GREEN,
                                                    typeName
                                                ),
                                                Texts.flit(
                                                    Formatting.GRAY,
                                                    ")"
                                                ),
                                                Texts.flit(
                                                    Formatting.WHITE,
                                                    " = "
                                                ),
                                                Texts.flit(
                                                    Formatting.LIGHT_PURPLE,
                                                    value
                                                )
                                            )
                                        )
                                    }
                                )
                                0
                            }
                    )
                )
                .then(
                    LiteralArgumentBuilder.literal<CommandSource>("set")
                        .then(
                            RequiredArgumentBuilder.argument<CommandSource, Module>(
                                "module",
                                moduleArgumentType
                            )
                                .then(
                                    RequiredArgumentBuilder.argument<CommandSource, Setting<Any>>(
                                        "setting",
                                        settingArgumentType
                                    )
                                        .then(
                                            RequiredArgumentBuilder.argument<CommandSource, String>(
                                                "value",
                                                SettingValueArgumentType.value(settingArgumentType, "setting", 1)
                                            )
                                                .executes { context: CommandContext<CommandSource> ->
                                                    val module =
                                                        context.getArgument(
                                                            "module",
                                                            Module::class.java
                                                        ) as Module
                                                    val setting =
                                                        context.getArgument(
                                                            "setting",
                                                            Setting::class.java
                                                        ) as Setting<Any>
                                                    val stringValue = context.getArgument(
                                                        "value",
                                                        String::class.java
                                                    ) as String
                                                    val value: Any
                                                    value = try {
                                                        setting.convertFromString(stringValue)
                                                    } catch (e: Exception) {
                                                        throw FAILED_EXCEPTION.create("Couldn't convert value from string to setting value (this shouldn't happen)")
                                                    }
                                                    setting.setValue(value)
                                                    (context.source as KamiCommandSource).sendFeedback(
                                                        Texts.f(
                                                            Formatting.GOLD,
                                                            Texts.append(
                                                                Texts.lit("Set property "),
                                                                Texts.flit(
                                                                    Formatting.YELLOW,
                                                                    setting.name
                                                                ),
                                                                Texts.lit(" of module "),
                                                                Texts.flit(
                                                                    Formatting.YELLOW,
                                                                    module.name.value
                                                                ),
                                                                Texts.lit(" to "),
                                                                Texts.flit(
                                                                    Formatting.LIGHT_PURPLE,
                                                                    setting.valueAsString
                                                                ),
                                                                Texts.lit("!")
                                                            )
                                                        )
                                                    )
                                                    0
                                                }
                                        )
                                )
                        )
                )
        )
    }
}