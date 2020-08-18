package me.zeroeightsix.kami.mixin.client;

import me.zeroeightsix.kami.feature.module.Timer;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderTickCounter.class)
public class MixinRenderTickCounter {

    @Shadow
    public float lastFrameDuration;

    @Inject(method = "beginRenderTick", at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/RenderTickCounter;lastFrameDuration:F"))
    public void onBeginRenderTick(long timeMillis, CallbackInfoReturnable<Integer> cir) {
        lastFrameDuration *= Timer.INSTANCE.getSpeedModifier();
    }

}
