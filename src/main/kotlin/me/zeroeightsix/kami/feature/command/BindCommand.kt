package me.zeroeightsix.kami.feature.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import me.zeroeightsix.kami.feature.Feature
import me.zeroeightsix.kami.feature.FullFeature
import me.zeroeightsix.kami.feature.HasBind
import me.zeroeightsix.kami.util.Bind
import me.zeroeightsix.kami.util.Texts
import net.minecraft.server.command.CommandSource
import net.minecraft.util.Formatting

object BindCommand : Command() {
    override fun register(dispatcher: CommandDispatcher<CommandSource>) {
        dispatcher.register(
            LiteralArgumentBuilder.literal<CommandSource>("bind")
                .then(
                    RequiredArgumentBuilder.argument<CommandSource, HasBind>(
                        "feature",
                        FeatureArgumentType.boundFeature()
                    )
                        .then(
                            RequiredArgumentBuilder.argument<CommandSource, Bind>("bind", BindArgumentType.bind())
                                .executes {
                                    val source = it.source as KamiCommandSource
                                    val feature = it.getArgument("feature", HasBind::class.java)
                                    val bind = it.getArgument("bind", Bind::class.java)
                                    feature.bind = bind
                                    sendFeedback(source, (feature as Feature).name, " bound to ", bind)
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
