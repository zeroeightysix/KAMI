package me.zeroeightsix.kami.event.events;

import me.zeroeightsix.kami.event.KamiEvent;
import net.minecraft.client.gui.hud.ClientBossBar;

public class RenderBossBarEvent extends KamiEvent {

    private final ClientBossBar bossBar;

    public RenderBossBarEvent(ClientBossBar bossBar) {
        this.bossBar = bossBar;
    }

    public ClientBossBar getBossBar() {
        return bossBar;
    }

}
