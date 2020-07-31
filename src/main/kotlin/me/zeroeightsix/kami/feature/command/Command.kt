package me.zeroeightsix.kami.feature.command

import com.mojang.brigadier.CommandDispatcher
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.feature.SimpleFeature
import me.zeroeightsix.kami.mc
import net.minecraft.server.command.CommandSource
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import java.util.regex.Pattern

abstract class Command : SimpleFeature(hidden = true) {

    abstract fun register(dispatcher: CommandDispatcher<CommandSource>)

    @Deprecated("")
    class ChatMessage(text: String?) : LiteralText(text) {
        var text: String
        override fun copy(): Text {
            return ChatMessage(text)
        }

        init {
            val p = Pattern.compile("&[0123456789abcdefrlonmk]")
            val m = p.matcher(text)
            val sb = StringBuffer()
            while (m.find()) {
                val replacement = "\u00A7" + m.group().substring(1)
                m.appendReplacement(sb, replacement)
            }
            m.appendTail(sb)
            this.text = sb.toString()
        }
    }

    companion object {
        @JvmField
        var dispatcher = CommandDispatcher<CommandSource>()

        @JvmField
        var SECTION_SIGN = '\u00A7'

        @JvmStatic
        @Deprecated("")
        fun sendChatMessage(message: String) {
            sendRawChatMessage("&7[&a" + KamiMod.KAMI_KANJI + "&7] &r" + message)
        }

        @Deprecated("")
        fun sendRawChatMessage(message: String?) {
            mc.player.sendMessage(ChatMessage(message))
        }
    }
}
