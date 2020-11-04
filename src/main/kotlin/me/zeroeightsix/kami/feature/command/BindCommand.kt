package me.zeroeightsix.kami.feature.command

import com.mojang.brigadier.CommandDispatcher
import me.zeroeightsix.kami.feature.Feature
import me.zeroeightsix.kami.feature.FullFeature
import me.zeroeightsix.kami.feature.HasBind
import me.zeroeightsix.kami.util.Bind
import me.zeroeightsix.kami.util.text
import net.minecraft.command.CommandSource
import net.minecraft.util.Formatting.GOLD
import net.minecraft.util.Formatting.LIGHT_PURPLE
import net.minecraft.util.Formatting.YELLOW

object BindCommand : Command() {
    override fun register(dispatcher: CommandDispatcher<CommandSource>) {
        dispatcher register rootLiteral("bind") {
            argument("feature", FeatureArgumentType.boundFeature()) {
                argument("bind", BindArgumentType.bind()) {
                    does { ctx ->
                        val feature: HasBind = "feature" from ctx
                        val bind: Bind = "bind" from ctx
                        feature.bind = bind
                        sendFeedback(ctx.source as KamiCommandSource, (feature as Feature).name, " bound to ", bind)
                        0
                    }
                }
                does {
                    val feature: FullFeature = "feature" from it
                    val bind = feature.bind
                    val featureName = feature.displayName
                    sendFeedback(it.source as KamiCommandSource, featureName, " is bound to ", bind)
                    0
                }
            }
        }
    }

    private fun sendFeedback(
        source: KamiCommandSource,
        name: String,
        infix: String,
        bind: Bind
    ) {
        source replyWith text(GOLD) {
            +"Feature "
            +name(YELLOW)
            +infix
            +bind.toString()(LIGHT_PURPLE)
            +"!"
        }
    }
}
