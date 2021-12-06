package me.zeroeightsix.kami.feature.module

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.PacketEvent
import net.minecraft.entity.passive.AbstractDonkeyEntity
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket

@Module.Info(
    name = "MountBypass",
    category = Module.Category.PLAYER,
    description = "Forcefully allows mounting donkeys with chests"
)
object MountBypass : Module() {
    @EventHandler
    private val listener = Listener({ event: PacketEvent.Send ->
        if (mc.player == null || mc.world == null || event.packet !is PlayerInteractEntityC2SPacket) return@Listener
        if (event.packet.getEntity(mc.server?.overworld) is AbstractDonkeyEntity) event.cancel()
    })
}