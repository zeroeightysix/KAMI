package me.zeroeightsix.kami.mixin.client;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix3f;
import net.minecraft.util.math.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MatrixStack.Entry.class)
public interface IMatrixStack$Entry {

    @Invoker("<init>")
    static MatrixStack.Entry create(Matrix4f model, Matrix3f normal) {
        throw new UnsupportedOperationException("Untransformed Accessor!");
    }

}
