package me.zeroeightsix.kami.feature.module

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.PacketEvent.Send
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket

@Module.Info(name = "PortalGodMode", category = Module.Category.PLAYER)
object PortalGodMode : Module() {
    var sent: TeleportConfirmC2SPacket? = null

    override fun onEnable() {
        sent = null
    }

    override fun onDisable() {
        sent?.let {
            mc.networkHandler?.sendPacket(sent)
        }
    }

    @EventHandler
    var listener = Listener({ event: Send ->
        if (event.packet is TeleportConfirmC2SPacket) {
            event.cancel()
            sent = event.packet
        }
    })
}
