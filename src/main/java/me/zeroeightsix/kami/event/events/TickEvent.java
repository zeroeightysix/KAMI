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

        /**
         * This exists because many listeners for TickEvents will perform player null checks.
         * This event is ensured to only fire when the player and world is not null.
         */
        public static class InGame extends Client {}

        /**
         * @see InGame
         */
        public static class OutOfGame extends Client {}
    }

}
