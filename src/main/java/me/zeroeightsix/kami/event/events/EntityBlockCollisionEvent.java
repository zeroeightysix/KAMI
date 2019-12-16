package me.zeroeightsix.kami.event.events;

import me.zeroeightsix.kami.event.KamiEvent;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class EntityBlockCollisionEvent extends KamiEvent {

    private final World world;
    private final BlockState state;
    private final BlockPos pos;
    private final Entity entity;

    public EntityBlockCollisionEvent(World world, BlockState state, BlockPos pos, Entity entity) {
        this.world = world;
        this.state = state;
        this.pos = pos;
        this.entity = entity;
    }

    public BlockState getState() {
        return state;
    }

    public Entity getEntity() {
        return entity;
    }

    public BlockPos getPos() {
        return pos;
    }

    public World getWorld() {
        return world;
    }

}
