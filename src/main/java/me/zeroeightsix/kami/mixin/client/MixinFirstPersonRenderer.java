package me.zeroeightsix.kami.mixin.client;

import me.zeroeightsix.kami.feature.module.NoRender;
import net.minecraft.client.render.FirstPersonRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FirstPersonRenderer.class)
public class MixinFirstPersonRenderer {
    @Inject(method = "renderFireOverlay", at = @At("HEAD"), cancellable = true)
    private void renderFireOverlay(CallbackInfo ci) {
        if (NoRender.INSTANCE.isEnabled() && NoRender.INSTANCE.getFire()) {
            ci.cancel();
        }
    }
}
