package me.zeroeightsix.kami.command.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import me.zeroeightsix.kami.command.Command
import me.zeroeightsix.kami.command.KamiCommandSource
import me.zeroeightsix.kami.plugin.PluginManager
import me.zeroeightsix.kami.util.Texts
import net.minecraft.server.command.CommandSource

object PluginCommand : Command() {

    override fun register(dispatcher: CommandDispatcher<CommandSource>?) {
        dispatcher!!.register(
            LiteralArgumentBuilder.literal<CommandSource>("plugins")
                .then(
                    LiteralArgumentBuilder.literal<CommandSource>("enable")
                        .then(RequiredArgumentBuilder.argument<CommandSource, String>("plugin", StringArgumentType.word())
                            .executes {
                                val plugin = PluginManager.getPlugin(it.getArgument("plugin", String::class.java))
                                val source = it.source as KamiCommandSource
                                if (plugin != null) {
                                    if (plugin.enabled) {
                                        source.sendFeedback(Texts.lit("It's already enabled."))
                                    } else {
                                        plugin.enable()
                                        source.sendFeedback(Texts.lit("Enabled!"))
                                    }
                                } else {
                                    source.sendFeedback(Texts.lit("that doesn't exist."))
                                }
                                0
                            })
                )
        )
    }

}