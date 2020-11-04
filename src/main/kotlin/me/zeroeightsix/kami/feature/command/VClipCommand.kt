package me.zeroeightsix.kami.feature.command

import com.mojang.brigadier.CommandDispatcher
import me.zeroeightsix.kami.mc
import net.minecraft.command.CommandSource

object VClipCommand : Command() {
    override fun register(dispatcher: CommandDispatcher<CommandSource>) {
        dispatcher register rootLiteral("vclip") {
            float("height") {
                does { ctx ->
                    mc.player?.let {
                        it.updatePosition(
                            it.x,
                            it.y + ("height".from<Float, CommandSource>(ctx)),
                            it.z
                        )
                    }
                    0
                }
            }
        }
    }
}
