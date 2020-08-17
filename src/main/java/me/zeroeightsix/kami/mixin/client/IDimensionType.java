package me.zeroeightsix.kami.mixin.client;

import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DimensionType.class)
public interface IDimensionType {

    @Accessor("OVERWORLD")
    static DimensionType getOverworld() {
        throw new UnsupportedOperationException("Untransformed mixin!");
    }

}
