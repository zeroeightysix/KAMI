package me.zeroeightsix.kami.feature.module

import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.BaritoneIntegration
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.event.PacketEvent
import me.zeroeightsix.kami.feature.module.Module
import me.zeroeightsix.kami.util.Friends
import me.zeroeightsix.kami.util.text.MessageDetection
import me.zeroeightsix.kami.util.text.MessageHelper
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket

@Module.Info(
    name = "RemoteCommand",
    description = "Execute commands sent from trusted players",
    category = Module.Category.MISC
)
object RemoteCommand : Module() {

    @Setting(comment = "Attempt repeating all messages, even if not a KAMI command")
    private var allMessages = false

    @Setting(comment = "Allow sending of Baritone commands")
    private var baritoneCommands = true

    @Setting(comment = "When disabled, anybody can send commands")
    private var friendsOnly = true

    private lateinit var listener: Listener<PacketEvent.Receive>

    init {
        hidden = true
        BaritoneIntegration {
            hidden = false

            listener = Listener({ event ->
                if (event.packet !is ChatMessageC2SPacket) return@Listener
                var message = event.packet.chatMessage

                if (MessageDetection.Direct.RECEIVE detectNot message) return@Listener

                val username = MessageDetection.Direct.RECEIVE.playerName(message) ?: return@Listener
                if (!isValidUser(username)) return@Listener

                message = MessageDetection.Direct.RECEIVE.removedOrNull(message)?.toString() ?: return@Listener

                MessageDetection.Command.KAMI.removedOrNull(message)?.let { command ->
                    MessageHelper.executeKamiCommand(command.toString())
                } ?: run {
                    MessageDetection.Command.BARITONE.removedOrNull(message)?.let { command ->
                        if (baritoneCommands) {
                            MessageHelper.executeBaritoneCommand(command.toString())
                        }
                    }
                } ?: run {
                    if (allMessages) {
                        mc.player?.sendChatMessage(message)
                    }
                }
            })
        }
    }

    override fun onEnable() {
        KamiMod.EVENT_BUS.subscribe(listener)
    }

    override fun onDisable() {
        KamiMod.EVENT_BUS.unsubscribe(listener)
    }

    private fun isValidUser(username: String): Boolean {
        return !friendsOnly || friendsOnly && Friends.isFriend(username)
    }
}
