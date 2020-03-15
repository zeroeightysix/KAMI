package me.zeroeightsix.kami.event.events;

import me.zeroeightsix.kami.event.KamiEvent;
import net.minecraft.client.network.AbstractClientPlayerEntity;

//TODO: Generify into RenderTagEvent
public class RenderPlayerNametagEvent extends KamiEvent {

    final AbstractClientPlayerEntity entity;

    public RenderPlayerNametagEvent(AbstractClientPlayerEntity entity) {
        this.entity = entity;
    }

    public AbstractClientPlayerEntity getEntity() {
        return entity;
    }

}
