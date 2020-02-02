package me.zeroeightsix.kami.command.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import me.zeroeightsix.kami.command.Command
import me.zeroeightsix.kami.command.KamiCommandSource
import me.zeroeightsix.kami.command.ModuleArgumentType
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.util.Texts
import net.minecraft.server.command.CommandSource
import net.minecraft.util.Formatting

/**
 * Created by 086 on 17/11/2017.
 */
object ToggleCommand : Command() {
    override fun register(dispatcher: CommandDispatcher<CommandSource>) {
        dispatcher.register(
            LiteralArgumentBuilder.literal<CommandSource>("toggle")
                .then(
                    RequiredArgumentBuilder.argument<CommandSource, Module>(
                        "module",
                        ModuleArgumentType.module()
                    )
                        .executes { context: CommandContext<CommandSource> ->
                            val m =
                                context.getArgument(
                                    "module",
                                    Module::class.java
                                )
                            m.toggle()
                            (context.source as KamiCommandSource).sendFeedback(
                                Texts.f(
                                    Formatting.GOLD, Texts.append(
                                        Texts.lit("Toggled module "),
                                        Texts.flit(
                                            Formatting.YELLOW,
                                            m.name.value
                                        ),
                                        Texts.lit(", now "),
                                        Texts.flit(
                                            if (m.isEnabled) Formatting.GREEN else Formatting.RED,
                                            if (m.isEnabled) "ON" else "OFF"
                                        )
                                    )
                                )
                            )
                            0
                        }
                )
        )
    }
}