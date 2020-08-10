package me.zeroeightsix.kami.feature.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import me.zeroeightsix.kami.feature.FullFeature
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
                    RequiredArgumentBuilder.argument<CommandSource, FullFeature>(
                        "feature",
                        FullFeatureArgumentType.feature()
                    )
                        .executes { context: CommandContext<CommandSource> ->
                            val f =
                                context.getArgument(
                                    "feature",
                                    FullFeature::class.java
                                )
                            f.toggle()
                            (context.source as KamiCommandSource).sendFeedback(
                                Texts.f(
                                    Formatting.GOLD, Texts.append(
                                        Texts.lit("Toggled feature "),
                                        Texts.flit(
                                            Formatting.YELLOW,
                                            f.name
                                        ),
                                        Texts.lit(", now "),
                                        Texts.flit(
                                            if (f.isEnabled()) Formatting.GREEN else Formatting.RED,
                                            if (f.isEnabled()) "ON" else "OFF"
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
