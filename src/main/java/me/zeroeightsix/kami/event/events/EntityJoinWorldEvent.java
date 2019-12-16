package me.zeroeightsix.kami.event.events;

import me.zeroeightsix.kami.event.KamiEvent;
import net.minecraft.entity.Entity;

public class EntityJoinWorldEvent extends KamiEvent {

    private final int id;
    private final Entity entity;

    public EntityJoinWorldEvent(int id, Entity entity) {
        this.id = id;
        this.entity = entity;
    }

    public Entity getEntity() {
        return entity;
    }

    public int getId() {
        return id;
    }

}
