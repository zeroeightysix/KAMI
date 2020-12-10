package me.zeroeightsix.kami.feature.module

import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting
import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.PacketEvent
import me.zeroeightsix.kami.gui.text.CompiledText
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket

@Module.Info(
    name = "AutoGG",
    category = Module.Category.MISC,
    description = "Says GG or a custom message at the end of a match"
)
object AutoGG : Module() {
    @Setting
    var ggMessage = CompiledText(mutableListOf())

    private const val updateLimit: Long = 2000L // 2 Seconds
    private var lastUpdate: Long = 0L

    private val triggers = listOf(
        "1st Killer - ",
        "1st Place - ",
        " - Damage Dealt - ",
        "Winning Team -",
        "1st - ",
        "Winners:",
        "Winner:",
        "Winning Team:",
        " won the game!",
        "Top Seeker:",
        "1st Place:",
        "Last team standing!",
        "Winner #1 (",
        "Top Survivors",
        "Winners - ",
        "Sumo Duel - ",
        " WINNER!"
    )

    @EventHandler
    var listener = Listener({ event: PacketEvent.Receive ->
        if (event.packet is GameMessageS2CPacket && event.packet.senderUuid.toString() == "00000000-0000-0000-0000-000000000000") {
            val chatMessage = event.packet.message.string
            triggers.forEach { trigger ->
                if (chatMessage.contains(trigger) && (lastUpdate + updateLimit <= System.currentTimeMillis())) {
                    mc.player?.sendChatMessage(ggMessage.toString())
                    lastUpdate = System.currentTimeMillis()
                }
            }
        }
    })
}
