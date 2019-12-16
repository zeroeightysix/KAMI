package me.zeroeightsix.kami.event.events;

import me.zeroeightsix.kami.event.KamiEvent;
import net.minecraft.client.gui.screen.Screen;

/**
 * Created by 086 on 17/11/2017.
 */
public class ScreenEvent extends KamiEvent  {

    private Screen screen;

    public ScreenEvent(Screen screen) {
        super();
        this.screen = screen;
    }

    public Screen getScreen() {
        return screen;
    }

    public void setScreen(Screen screen) {
        this.screen = screen;
    }

    public static class Displayed extends ScreenEvent {
        public Displayed(Screen screen) {
            super(screen);
        }
    }

    public static class Closed extends ScreenEvent {
        public Closed(Screen screen) {
            super(screen);
        }
    }

}
