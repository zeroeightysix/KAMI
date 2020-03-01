package me.zeroeightsix.kami.module.modules.misc;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.events.ScreenEvent;
import me.zeroeightsix.kami.mixin.client.IDisconnectedScreen;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraft.client.gui.screen.ConnectScreen;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.options.ServerEntry;

/**
 * Created by 086 on 9/04/2018.
 */
@Module.Info(name = "AutoReconnect", description = "Automatically reconnects after being disconnected", category = Module.Category.MISC, alwaysListening = true)
public class AutoReconnect extends Module {

    private Setting<Integer> seconds = register(Settings.integerBuilder("Seconds").withValue(5).withMinimum(0).build());
    private static ServerEntry cServer;

    @EventHandler
    public Listener<ScreenEvent.Closed> closedListener = new Listener<>(event -> {
        if (event.getScreen() instanceof ConnectScreen)
            cServer = mc.getCurrentServerEntry();
    });

    @EventHandler
    public Listener<ScreenEvent.Displayed> displayedListener = new Listener<>(event -> {
        if (isEnabled() && event.getScreen() instanceof DisconnectedScreen && (cServer != null || mc.getCurrentServerEntry() != null))
            event.setScreen(new KamiDisconnectedScreen((DisconnectedScreen) event.getScreen()));
    });

    private class KamiDisconnectedScreen extends DisconnectedScreen {
        private Screen parent;
        int millis = seconds.getValue() * 1000;
        long cTime;

        public KamiDisconnectedScreen(DisconnectedScreen disconnected) {
            super(((IDisconnectedScreen) disconnected).getParent(), disconnected.getTitle().asString(), ((IDisconnectedScreen) disconnected).getReason());
            cTime = System.currentTimeMillis();
            parent = ((IDisconnectedScreen) disconnected).getParent();
        }

        @Override
        public void tick() {
            if (millis <= 0)
                mc.openScreen(new ConnectScreen(parent, mc, cServer == null ? mc.getCurrentServerEntry() : cServer));
        }

        @Override
        public void render(int mouseX, int mouseY, float partialTicks) {
            super.render(mouseX, mouseY, partialTicks);

            long a = System.currentTimeMillis();
            millis -= a - cTime;
            cTime = a;

            String s = "Reconnecting in " + Math.max(0, Math.floor((double) millis / 100) / 10) + "s";
            font.drawWithShadow(s, width / 2 - font.getStringWidth(s) / 2, height - 16, 0xffffff);
        }

    }

}
