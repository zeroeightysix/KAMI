package me.zeroeightsix.kami.feature.module

import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting
import java.util.UUID
import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.PacketEvent
import me.zeroeightsix.kami.gui.ImguiDSL.imgui
import me.zeroeightsix.kami.gui.text.CompiledText
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket

@Module.Info(
    name = "AutoGG",
    category = Module.Category.MISC,
    description = "Says GG or a custom message at the end of a match"
)
object AutoGG : Module() {
    @Setting
    var ggMessage = CompiledText(
        mutableListOf(
            CompiledText.LiteralPart("gg".imgui)
        )
    )

    private const val updateLimit: Long = 7000 // 7 Seconds
    private var lastUpdate: Long = 0L
    private final val emptyUuid = UUID(0, 0) // Nil UUID

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
    private val listener = Listener({ event: PacketEvent.Receive ->
        if (lastUpdate + updateLimit <= System.currentTimeMillis() &&
            event.packet is GameMessageS2CPacket &&
            event.packet.sender == emptyUuid
        ) {
            val chatMessage = event.packet.message.string
            if (triggers.any { chatMessage.contains(it, ignoreCase = false) }) {
                mc.player?.sendChatMessage(ggMessage.toString())
                lastUpdate = System.currentTimeMillis()
            }
        }
    })
}