package me.zeroeightsix.kami.mixin.client;

import io.github.fablabsmc.fablabs.impl.fiber.annotation.BackedConfigLeaf;
import me.zeroeightsix.kami.mixin.duck.CanDisableCaching;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = BackedConfigLeaf.class, remap = false)
public class MixinBackedConfigLeaf<R, S> implements CanDisableCaching {

    @Shadow
    private R cachedValue;
    private boolean cachingDisabled = false;

    @Override
    public boolean isCachingDisabled() {
        return this.cachingDisabled;
    }

    @Override
    public void setCachingDisabled(boolean disabled) {
        this.cachingDisabled = disabled;
    }

    @Inject(method = "getValue", at = @At("RETURN"))
    public void onReturnGetValue(CallbackInfoReturnable<?> returnable) {
        if (cachingDisabled)
            this.cachedValue = null;
    }

}
