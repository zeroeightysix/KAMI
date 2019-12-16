package me.zeroeightsix.kami.mixin.extend;

import net.minecraft.client.input.Input;

public interface ExtendedInput {

    Input copy();
    void update(Input from);
    void update(float movementSideways, float movementForward, boolean pressingForward, boolean pressingBack, boolean pressingLeft, boolean pressingRight, boolean jumping, boolean sneaking);

}
