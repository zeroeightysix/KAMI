package me.zeroeightsix.kami.mixin.client;

import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.event.events.RenderPlayerNametagEvent;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Created by 086 on 19/12/2017.
 */
@Mixin(PlayerEntityRenderer.class)
public class MixinRenderPlayer {

    @Inject(method = "method_4213", at = @At("HEAD"), cancellable = true)
    public void renderLivingLabel(AbstractClientPlayerEntity entityIn, double x, double y, double z, String name, double distanceSq, CallbackInfo info) {
        RenderPlayerNametagEvent event = new RenderPlayerNametagEvent(entityIn);
        KamiMod.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            info.cancel();
        }
    }

}
