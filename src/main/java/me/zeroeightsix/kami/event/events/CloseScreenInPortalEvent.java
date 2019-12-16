package me.zeroeightsix.kami.event.events;

import me.zeroeightsix.kami.event.KamiEvent;
import net.minecraft.client.network.ClientPlayerEntity;

public class CloseScreenInPortalEvent extends KamiEvent {

    final ClientPlayerEntity entity;

    public CloseScreenInPortalEvent(ClientPlayerEntity entity) {
        this.entity = entity;
    }

    public ClientPlayerEntity getEntity() {
        return entity;
    }

}
