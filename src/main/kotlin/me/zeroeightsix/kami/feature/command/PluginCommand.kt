package me.zeroeightsix.kami.feature.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import me.zeroeightsix.kami.feature.plugin.Plugin
import me.zeroeightsix.kami.util.Texts.*
import net.minecraft.server.command.CommandSource
import net.minecraft.text.LiteralText
import net.minecraft.util.Formatting.GOLD
import net.minecraft.util.Formatting.YELLOW
import java.util.function.Function

object PluginCommand : Command() {

    private val failedException = DynamicCommandExceptionType(
        Function { o: Any -> LiteralText(o.toString()) }
    )

    override fun register(dispatcher: CommandDispatcher<CommandSource>) {
        dispatcher.register(
            LiteralArgumentBuilder.literal<CommandSource>("plugins")
                .then(
                    LiteralArgumentBuilder.literal<CommandSource>("enable")
                        .then(RequiredArgumentBuilder.argument<CommandSource, Plugin>("plugin", PluginArgumentType.plugin())
                            .executes {
                                continueIfExists(
                                    it
                                )?.let {
                                    toggle(
                                        "enable",
                                        it,
                                        true
                                    )
                                }
                                0
                            })
                )
                .then(
                    LiteralArgumentBuilder.literal<CommandSource>("disable")
                        .then(RequiredArgumentBuilder.argument<CommandSource, Plugin>("plugin", PluginArgumentType.plugin())
                            .executes {
                                continueIfExists(
                                    it
                                )?.let {
                                    toggle(
                                        "disable",
                                        it,
                                        false
                                    )
                                }
                                0
                            })
                )
        )
    }

    private fun continueIfExists(context: CommandContext<CommandSource>): Pair<KamiCommandSource, Plugin>? {
        val plugin = context.getArgument("plugin", Plugin::class.java)
        val source = context.source as KamiCommandSource
        if (plugin == null) {
            throw failedException.create("Couldn't find that plugin.")
        }
        return Pair(source, plugin)
    }

    // thank english grammar for allowing us to use one parameter for word
    private fun toggle(word: String, pair: Pair<KamiCommandSource, Plugin>, enable: Boolean) {
        val (source, plugin) = pair
        if (plugin.enabled.value != enable) {
            plugin.enabled.value = enable
            source.sendFeedback(f(GOLD, append(
                lit("${word.capitalize()}d plugin "),
                flit(YELLOW, plugin.name.value)
            )))
        }
        else
            source.sendFeedback(f(GOLD, append(
                flit(YELLOW, plugin.name.value),
                lit(" is already ${word}d.")
            )))
    }

}