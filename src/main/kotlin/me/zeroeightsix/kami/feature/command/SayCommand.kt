package me.zeroeightsix.kami.feature.command

import com.mojang.brigadier.CommandDispatcher
import me.zeroeightsix.kami.mc
import net.minecraft.server.command.CommandSource

object SayCommand : Command() {
    override fun register(dispatcher: CommandDispatcher<CommandSource>) {
        dispatcher register rootLiteral("say") {
            greedyString("message") {
                does {
                    mc.player?.sendChatMessage("message" from it)
                    0
                }
            }
        }
    }
}

