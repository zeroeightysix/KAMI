package me.zeroeightsix.kami.command.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.command.Command
import me.zeroeightsix.kami.command.KamiCommandSource
import me.zeroeightsix.kami.util.Texts
import net.minecraft.server.command.CommandSource
import net.minecraft.text.LiteralText
import net.minecraft.util.Formatting
import java.util.function.Function

/**
 * Created by 086 on 14/10/2018.
 */
object ConfigCommand : Command() {
    private val FAILED_EXCEPTION =
        DynamicCommandExceptionType(Function { o: Any ->
            LiteralText(o.toString())
        })

    override fun register(dispatcher: CommandDispatcher<CommandSource>) {
        dispatcher.register(
            LiteralArgumentBuilder.literal<CommandSource>("config")
                .then(
                    LiteralArgumentBuilder.literal<CommandSource>("reload")
                        .executes { context: CommandContext<CommandSource> ->
                            KamiMod.loadConfiguration()
                            (context.source as KamiCommandSource).sendFeedback(
                                Texts.f(
                                    Formatting.GOLD, Texts.append(
                                        Texts.lit("Reloaded configuration "),
                                        Texts.flit(
                                            Formatting.YELLOW,
                                            KamiMod.getConfigName()
                                        ),
                                        Texts.lit("!")
                                    )
                                )
                            )
                            0
                        }
                )
                .then(
                    LiteralArgumentBuilder.literal<CommandSource>("save")
                        .executes { context: CommandContext<CommandSource> ->
                            KamiMod.saveConfiguration()
                            (context.source as KamiCommandSource).sendFeedback(
                                Texts.f(
                                    Formatting.GOLD, Texts.append(
                                        Texts.lit("Saved configuration "),
                                        Texts.flit(
                                            Formatting.YELLOW,
                                            KamiMod.getConfigName()
                                        ),
                                        Texts.lit("!")
                                    )
                                )
                            )
                            0
                        }
                )
                .then(
                    LiteralArgumentBuilder.literal<CommandSource>("switch")
                        .then(
                            RequiredArgumentBuilder.argument<CommandSource, String>(
                                "filename",
                                StringArgumentType.string()
                            )
                                .executes { context: CommandContext<CommandSource> ->
                                    val filename =
                                        context.getArgument("filename", String::class.java)
                                    if (!KamiMod.isFilenameValid(filename)) {
                                        throw FAILED_EXCEPTION.create("Invalid filename '$filename'!")
                                    }
                                    val source = context.source as KamiCommandSource
                                    KamiMod.saveConfiguration()
                                    source.sendFeedback(
                                        Texts.f(
                                            Formatting.GOLD, Texts.append(
                                                Texts.lit("Saved "),
                                                Texts.flit(
                                                    Formatting.YELLOW,
                                                    KamiMod.getConfigName()
                                                )
                                            )
                                        )
                                    )
                                    KamiMod.setLastConfigName(filename)
                                    source.sendFeedback(
                                        Texts.f(
                                            Formatting.GOLD, Texts.append(
                                                Texts.lit("Set "),
                                                Texts.flit(
                                                    Formatting.YELLOW,
                                                    "KAMILastConfig.txt"
                                                ),
                                                Texts.lit(" to "),
                                                Texts.flit(
                                                    Formatting.LIGHT_PURPLE,
                                                    filename
                                                )
                                            )
                                        )
                                    )
                                    KamiMod.loadConfiguration()
                                    source.sendFeedback(
                                        Texts.f(
                                            Formatting.GOLD, Texts.append(
                                                Texts.lit("Loaded "),
                                                Texts.flit(
                                                    Formatting.YELLOW,
                                                    filename
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
    }
}