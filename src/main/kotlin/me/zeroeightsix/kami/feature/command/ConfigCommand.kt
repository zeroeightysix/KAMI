package me.zeroeightsix.kami.feature.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.setting.KamiConfig
import me.zeroeightsix.kami.util.Texts
import net.minecraft.server.command.CommandSource
import net.minecraft.text.LiteralText
import net.minecraft.util.Formatting
import java.lang.Exception
import java.nio.file.Paths
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
                            try {
                                KamiConfig.loadConfiguration(KamiMod.config)
                            } catch (e: Exception) {
                                throw FAILED_EXCEPTION.create(e.message)
                            }
                            (context.source as KamiCommandSource).sendFeedback(
                                Texts.flit(Formatting.GOLD, "Reloaded configuration!")
                            )
                            0
                        }
                )
                .then(
                    LiteralArgumentBuilder.literal<CommandSource>("save")
                        .executes { context: CommandContext<CommandSource> ->
                            KamiConfig.saveConfiguration(KamiMod.config)
                            (context.source as KamiCommandSource).sendFeedback(
                                Texts.flit(Formatting.GOLD, "Saved configuration!")
                            )
                            0
                        }
                )
                .then(
                    LiteralArgumentBuilder.literal<CommandSource>("where")
                        .executes {
                            val path = Paths.get(KamiConfig.CONFIG_FILENAME)
                            (it.source as KamiCommandSource).sendFeedback(
                                Texts.append(
                                    Texts.flit(Formatting.GOLD, "The configuration file is at "),
                                    Texts.flit(Formatting.YELLOW, path.toAbsolutePath().toString())
                                )
                            )
                            0
                        }
                )
        )
    }
}