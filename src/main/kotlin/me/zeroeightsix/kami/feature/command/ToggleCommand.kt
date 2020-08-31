package me.zeroeightsix.kami.feature.command

import com.mojang.brigadier.CommandDispatcher
import me.zeroeightsix.kami.feature.FullFeature
import me.zeroeightsix.kami.to
import me.zeroeightsix.kami.util.Texts
import net.minecraft.server.command.CommandSource
import net.minecraft.util.Formatting

object ToggleCommand : Command() {
    override fun register(dispatcher: CommandDispatcher<CommandSource>) {
        dispatcher register rootLiteral("toggle") {
            argument("feature", FeatureArgumentType.fullFeature()) {
                does { ctx ->
                    val f: FullFeature = "feature" from ctx
                    f.enabled = !f.enabled
                    (ctx.source as KamiCommandSource).sendFeedback(
                        Texts.f(
                            Formatting.GOLD, Texts.append(
                                Texts.lit("Toggled feature "),
                                Texts.flit(
                                    Formatting.YELLOW,
                                    f.name
                                ),
                                Texts.lit(", now "),
                                Texts.flit(
                                    f.enabled.to(Formatting.GREEN, Formatting.RED),
                                    f.enabled.to("ON", "OFF")
                                )
                            )
                        )
                    )
                    0
                }
            }
        }
    }
}
