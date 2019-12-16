package me.zeroeightsix.kami.event.events;

import me.zeroeightsix.kami.event.KamiEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

public class LivingEntityTickEvent extends KamiEvent {

    private final LivingEntity entity;

    public LivingEntityTickEvent(LivingEntity entity) {
        this.entity = entity;
    }

    public Entity getEntity() {
        return entity;
    }

}
