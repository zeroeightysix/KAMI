package me.zeroeightsix.kami.event.events;

import me.zeroeightsix.kami.event.KamiEvent;
import net.minecraft.entity.MovementType;
import net.minecraft.util.math.Vec3d;

/**
 * @author 086
 */
public class PlayerMoveEvent extends KamiEvent {

    private final MovementType type;
    private final Vec3d vec;

    public PlayerMoveEvent(MovementType type, Vec3d vec) {
        this.type = type;
        this.vec = vec;
    }

    public MovementType getType() {
        return type;
    }

    public Vec3d getVec() {
        return vec;
    }

}
