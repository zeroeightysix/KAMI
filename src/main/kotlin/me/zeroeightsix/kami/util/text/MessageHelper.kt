package me.zeroeightsix.kami.util.text

import baritone.api.event.events.ChatEvent
import com.mojang.brigadier.exceptions.CommandSyntaxException
import me.zeroeightsix.kami.BaritoneIntegration
import me.zeroeightsix.kami.feature.command.Command
import me.zeroeightsix.kami.feature.command.KamiCommandSource
import me.zeroeightsix.kami.gui.windows.Settings
import me.zeroeightsix.kami.util.Wrapper
import me.zeroeightsix.kami.util.text
import net.minecraft.text.LiteralText
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Formatting.AQUA
import net.minecraft.util.Formatting.DARK_PURPLE
import net.minecraft.util.Formatting.LIGHT_PURPLE
import java.util.*

object MessageHelper {

    fun executeKamiCommand(commandIn: String, trim: Boolean = false) {
        val command =
            if (trim) commandIn.removePrefix(Settings.commandPrefix.toString())
            else commandIn

        try {
            Command.dispatcher.execute(
                command,
                KamiCommandSource(Wrapper.getMinecraft().networkHandler, Wrapper.getMinecraft())
            )
        } catch (e: CommandSyntaxException) {
            sendMessage(LiteralText(e.message).setStyle(Style.EMPTY.withColor(Formatting.RED)))
        }
    }

    fun executeBaritoneCommand(command: String) {
        BaritoneIntegration {
            val chatControl = BaritoneIntegration.settings!!.chatControl
            val prevValue = chatControl.value
            chatControl.value = true // if someone disabled this setting we still want to be able to send commands

            val event = ChatEvent(command)
            BaritoneIntegration.primary!!.gameEventHandler.onSendChatMessage(event)

            if (!event.isCancelled && !command.split(" ").firstOrNull().equals("damn", true)) {
                sendMessage(
                    text {
                        +"["(DARK_PURPLE)
                        +"Baritone"(LIGHT_PURPLE)
                        +"]"(DARK_PURPLE)
                        +" Invalid Command! Please view possible commands at "
                        +"https://github.com/cabaletta/baritone"(AQUA)
                    }
                )
            }

            chatControl.value = prevValue
        }
    }

    fun sendMessage(text: Text) {
        Wrapper.getPlayer().sendSystemMessage(text, UUID.randomUUID())
    }

}