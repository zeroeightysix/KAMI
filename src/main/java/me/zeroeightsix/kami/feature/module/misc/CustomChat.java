package me.zeroeightsix.kami.feature.module.misc;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.events.PacketEvent;
import me.zeroeightsix.kami.mixin.client.IChatMessageC2SPacket;
import me.zeroeightsix.kami.feature.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraft.server.network.packet.ChatMessageC2SPacket;

/**
 * Created by 086 on 8/04/2018.
 */
@Module.Info(name = "CustomChat", category = Module.Category.MISC, description = "Modifies your chat messages")
public class CustomChat extends Module {

    private Setting<Boolean> commands = register(Settings.b("Commands", false));

    private final String KAMI_SUFFIX = " \u23D0 \u1D0B\u1D00\u1D0D\u026A";

    @EventHandler
    public Listener<PacketEvent.Send> listener = new Listener<>(event -> {
        if (event.getPacket() instanceof ChatMessageC2SPacket) {
            String s = ((IChatMessageC2SPacket) event.getPacket()).getChatMessage();
            if (s.startsWith("/") && !commands.getValue()) return;
            s += KAMI_SUFFIX;
            if (s.length() >= 256) s = s.substring(0,256);
            ((IChatMessageC2SPacket) event.getPacket()).setChatMessage(s);
        }
    });

}
