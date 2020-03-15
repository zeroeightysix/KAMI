package me.zeroeightsix.kami.event.events;

import me.zeroeightsix.kami.event.KamiEvent;
import net.minecraft.client.gui.screen.Screen;

public class CloseScreenInPortalEvent extends KamiEvent {

    final Screen screen;

    public CloseScreenInPortalEvent(Screen screen) {
        this.screen = screen;
    }

    public Screen getScreen() {
        return screen;
    }

}
