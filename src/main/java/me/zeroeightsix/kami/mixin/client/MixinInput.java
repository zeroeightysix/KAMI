package me.zeroeightsix.kami.mixin.client;

import me.zeroeightsix.kami.mixin.extend.ExtendedInput;
import net.minecraft.client.input.Input;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Input.class)
public class MixinInput implements ExtendedInput {

    @Shadow
    public float movementSideways;
    @Shadow
    public float movementForward;
    @Shadow
    public boolean pressingForward;
    @Shadow
    public boolean pressingBack;
    @Shadow
    public boolean pressingLeft;
    @Shadow
    public boolean pressingRight;
    @Shadow
    public boolean jumping;
    @Shadow
    public boolean sneaking;

    @Override
    public Input copy() {
        Input copy = new Input();
        ((ExtendedInput) copy).update(movementSideways, movementForward, pressingForward, pressingBack, pressingLeft, pressingRight, jumping, sneaking);
        return copy;
    }

    @Override
    public void update(Input from) {
        this.update(from.movementSideways, from.movementForward, from.pressingForward, from.pressingBack, from.pressingLeft, from.pressingRight, from.jumping, from.sneaking);
    }

    @Override
    public void update(float movementSideways, float movementForward, boolean pressingForward, boolean pressingBack, boolean pressingLeft, boolean pressingRight, boolean jumping, boolean sneaking) {
        this.movementForward = movementForward;
        this.movementSideways = movementSideways;
        this.pressingForward = pressingForward;
        this.pressingBack = pressingBack;
        this.pressingLeft = pressingLeft;
        this.pressingRight = pressingRight;
        this.jumping = jumping;
        this.sneaking = sneaking;
    }
}
