package me.zeroeightsix.kami.module.modules.player;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.events.PacketEvent;
import me.zeroeightsix.kami.module.Module;
import net.minecraft.server.network.packet.TeleportConfirmC2SPacket;

/**
 * Created by GlowskiBroski on 10/14/2018.
 */
@Module.Info(name = "PortalGodMode", category = Module.Category.PLAYER)
public class PortalGodMode extends Module {

    @EventHandler
    public Listener<PacketEvent.Send> listener = new Listener<>(event -> {
        if (isEnabled() && event.getPacket() instanceof TeleportConfirmC2SPacket) {
            event.cancel();
        }
    });

}
