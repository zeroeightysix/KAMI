package me.zeroeightsix.kami.mixin.client;

import com.google.common.collect.ImmutableList;
import me.zeroeightsix.kami.feature.module.NoRender;
import net.minecraft.block.entity.BeaconBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(BeaconBlockEntity.class)
public class MixinBeaconBlockEntity {
    @Inject(method = "getBeamSegments", at = @At("RETURN"), cancellable = true)
    public void shouldBeamRender(CallbackInfoReturnable<List<BeaconBlockEntity.BeamSegment>> cir) {
        if (NoRender.INSTANCE.getEnabled() && NoRender.INSTANCE.getBeaconBeams()) {
            cir.setReturnValue(ImmutableList.of());
        }
    }
}
