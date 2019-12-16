package me.zeroeightsix.kami.event.events;

import me.zeroeightsix.kami.event.KamiEvent;
import net.minecraft.entity.Entity;

public class CanBeSteeredEvent extends KamiEvent {

    private final Entity entity;
    boolean canBeSteered;

    public CanBeSteeredEvent(Entity entity, boolean canBeSteered) {
        this.entity = entity;
        this.canBeSteered = canBeSteered;
    }

    public Entity getEntity() {
        return entity;
    }

    public boolean canBeSteered() {
        return canBeSteered;
    }

    public void setCanBeSteered(boolean canBeSteered) {
        this.canBeSteered = canBeSteered;
    }
}
