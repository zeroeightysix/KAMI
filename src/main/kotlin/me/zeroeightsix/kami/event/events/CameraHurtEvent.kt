package me.zeroeightsix.kami.event.events;

import me.zeroeightsix.kami.event.KamiEvent;

public class CameraHurtEvent extends KamiEvent {

    final float tickDelta;

    public CameraHurtEvent(float tickDelta) {
        this.tickDelta = tickDelta;
    }

}
