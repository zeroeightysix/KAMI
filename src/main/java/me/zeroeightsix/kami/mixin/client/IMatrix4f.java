package me.zeroeightsix.kami.mixin.client;

import net.minecraft.util.math.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Matrix4f.class)
public interface IMatrix4f {

    @Accessor
    float getA00();

    @Accessor
    float getA01();

    @Accessor
    float getA02();

    @Accessor
    float getA03();

    @Accessor
    float getA10();

    @Accessor
    float getA11();

    @Accessor
    float getA12();

    @Accessor
    float getA13();

    @Accessor
    float getA20();

    @Accessor
    float getA21();

    @Accessor
    float getA22();

    @Accessor
    float getA23();

    @Accessor
    float getA30();

    @Accessor
    float getA31();

    @Accessor
    float getA32();

    @Accessor
    float getA33();

}
