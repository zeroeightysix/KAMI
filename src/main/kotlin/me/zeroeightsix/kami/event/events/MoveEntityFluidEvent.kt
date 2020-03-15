package me.zeroeightsix.kami.event.events;

import me.zeroeightsix.kami.event.KamiEvent;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

public class MoveEntityFluidEvent extends KamiEvent {

    private final Entity entity;
    private Vec3d movement;

    public MoveEntityFluidEvent(Entity entity, Vec3d movement) {
        this.entity = entity;
        this.movement = movement;
    }

    public Entity getEntity() {
        return entity;
    }

    public Vec3d getMovement() {
        return movement;
    }

    public void setMovement(Vec3d movement) {
        this.movement = movement;
    }

}
