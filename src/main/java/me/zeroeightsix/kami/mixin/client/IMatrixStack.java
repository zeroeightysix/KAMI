package me.zeroeightsix.kami.mixin.client;

import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Deque;

@Mixin(MatrixStack.class)
public interface IMatrixStack {

    @Accessor
    Deque<MatrixStack.Entry> getStack();

}
