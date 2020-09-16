package me.zeroeightsix.kami.feature.command

import com.mojang.brigadier.CommandDispatcher
import me.zeroeightsix.kami.feature.FullFeature
import me.zeroeightsix.kami.util.text
import net.minecraft.server.command.CommandSource
import net.minecraft.util.Formatting
import net.minecraft.util.Formatting.GOLD
import net.minecraft.util.Formatting.YELLOW

object ToggleCommand : Command() {
    override fun register(dispatcher: CommandDispatcher<CommandSource>) {
        dispatcher register rootLiteral("toggle") {
            argument("feature", FeatureArgumentType.fullFeature()) {
                does { ctx ->
                    val f: FullFeature = "feature" from ctx
                    f.enabled = !f.enabled
                    ctx replyWith text(GOLD) {
                        +"Toggled feature "
                        +f.name(YELLOW)
                        +", now "
                        +(if (f.enabled) "ON" else "OFF")(if (f.enabled) Formatting.GREEN else Formatting.RED)
                    }
                    0
                }
            }
        }
    }
}
