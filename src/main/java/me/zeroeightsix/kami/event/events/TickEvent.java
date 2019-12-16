package me.zeroeightsix.kami.event.events;

import me.zeroeightsix.kami.event.KamiEvent;

public class TickEvent extends KamiEvent {

    public enum Stage {
        CLIENT
    }

    private final Stage stage;

    private TickEvent(Stage stage) {
        this.stage = stage;
    }

    public static class Client extends TickEvent {
        public Client() {
            super(Stage.CLIENT);
        }
    }

}
