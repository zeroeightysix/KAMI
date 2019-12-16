package me.zeroeightsix.kami.event.events;

import me.zeroeightsix.kami.event.KamiEvent;
import net.minecraft.client.input.Input;

public class InputUpdateEvent extends KamiEvent {

    final Input previousState;
    Input newState;

    public InputUpdateEvent(Input previousState, Input newState) {
        this.previousState = previousState;
        this.newState = newState;
    }

    public Input getNewState() {
        return newState;
    }

    public Input getPreviousState() {
        return previousState;
    }

    public void setNewState(Input newState) {
        this.newState = newState;
    }

}
