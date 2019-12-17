package me.zeroeightsix.kami.command;

import me.zeroeightsix.kami.util.Wrapper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientCommandSource;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.text.Text;

public class KamiCommandSource extends ClientCommandSource {
    public KamiCommandSource(ClientPlayNetworkHandler client, MinecraftClient minecraftClient) {
        super(client, minecraftClient);
    }

    public void sendFeedback(Text text) {
        Wrapper.getPlayer().sendMessage(text);
    }
}
