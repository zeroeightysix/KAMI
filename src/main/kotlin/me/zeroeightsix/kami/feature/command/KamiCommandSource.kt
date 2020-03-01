package me.zeroeightsix.kami.feature.command;

import me.zeroeightsix.kami.util.Wrapper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientCommandSource;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class KamiCommandSource extends ClientCommandSource {
    public KamiCommandSource(ClientPlayNetworkHandler client, MinecraftClient minecraftClient) {
        super(client, minecraftClient);
    }

    public void sendFeedback(Text text) {
        Wrapper.getPlayer().sendMessage(text);
    }

    public ServerCommandSource asServerSource() {
        return new ServerCommandSource(new KamiCommandOutput(this), Wrapper.getPlayer().getPos(), Wrapper.getPlayer().getRotationClient(), null, -1, Wrapper.getPlayer().getName().getString(), Wrapper.getPlayer().getName(), Wrapper.getMinecraft().getServer(), Wrapper.getPlayer());
    }

    private class KamiCommandOutput implements CommandOutput {
        private final KamiCommandSource source;

        public KamiCommandOutput(KamiCommandSource source) {
            this.source = source;
        }

        @Override
        public void sendMessage(Text message) {
            source.sendFeedback(message);
        }

        @Override
        public boolean sendCommandFeedback() {
            return true;
        }

        @Override
        public boolean shouldTrackOutput() {
            return false;
        }

        @Override
        public boolean shouldBroadcastConsoleToOps() {
            return false;
        }
    }
}
