package me.zeroeightsix.kami.event.events;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class PlayerAttackBlockEvent {

    final BlockPos blockPos;
    final Direction facing;

    public PlayerAttackBlockEvent(BlockPos blockPos, Direction facing) {
        this.blockPos = blockPos;
        this.facing = facing;
    }

    public BlockPos getPosition() {
        return blockPos;
    }

    public Direction getFacing() {
        return facing;
    }

}
