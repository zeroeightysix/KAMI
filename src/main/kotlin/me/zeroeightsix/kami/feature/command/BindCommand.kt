package me.zeroeightsix.kami.feature.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import me.zeroeightsix.kami.feature.FullFeature
import me.zeroeightsix.kami.feature.hidden.ClickGui
import me.zeroeightsix.kami.util.Bind
import me.zeroeightsix.kami.util.Texts
import net.minecraft.server.command.CommandSource
import net.minecraft.util.Formatting

object BindCommand : Command() {
    override fun register(dispatcher: CommandDispatcher<CommandSource>) {
        dispatcher.register(LiteralArgumentBuilder.literal<CommandSource>("bind")
            .then(RequiredArgumentBuilder.argument<CommandSource, FullFeature>(
                "feature",
                FullFeatureArgumentType.feature()
            )
                .then(RequiredArgumentBuilder.argument<CommandSource, Bind>("bind", BindArgumentType.bind())
                    .executes {
                        val source = it.source as KamiCommandSource
                        val feature = it.getArgument("feature", FullFeature::class.java)
                        val bind = it.getArgument("bind", Bind::class.java)
                        feature.bind = bind
                        sendFeedback(source, feature.displayName, " bound to ", bind)
                        0
                    }
                )
                .executes {
                    val source = it.source as KamiCommandSource
                    val feature = it.getArgument("feature", FullFeature::class.java)
                    val bind = feature.bind
                    val featureName = feature.displayName
                    sendFeedback(source, featureName, " is bound to ", bind)
                    0
                }
            )
            .then(LiteralArgumentBuilder.literal<CommandSource>(ClickGui.name)
                .then(RequiredArgumentBuilder.argument<CommandSource, Bind>("bind", BindArgumentType.bind())
                    .executes {
                        val source = it.source as KamiCommandSource
                        val feature = ClickGui
                        val bind = it.getArgument("bind", Bind::class.java)
                        feature.bind = bind
                        sendFeedback(source, ClickGui.name, " bound to ", bind)
                        0
                    }
                )
                .executes {
                    val source = it.source as KamiCommandSource
                    val bind = ClickGui.bind
                    val featureName = ClickGui.name
                    sendFeedback(source, featureName, " is bound to ", bind)
                    0
                }
            )
        )
    }

    private fun sendFeedback(
        source: KamiCommandSource,
        name: String,
        inbetween: String,
        bind: Bind
    ) {
        source.sendFeedback(
            Texts.f(
                Formatting.GOLD,
                Texts.append(
                    Texts.lit("Feature "),
                    Texts.flit(Formatting.YELLOW, name),
                    Texts.lit(inbetween),
                    Texts.flit(Formatting.LIGHT_PURPLE, bind.toString()),
                    Texts.lit("!")
                )
            )
        )
    }
}
