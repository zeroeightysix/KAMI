package me.zeroeightsix.kami.mixin.client;

import me.zeroeightsix.kami.feature.module.Nametags;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public class MixinEntityRenderer<T extends Entity> {

    @Inject(method = "renderLabelIfPresent", at = @At("HEAD"), cancellable = true)
    public void onRenderLabelIfPresent(T entity, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (Nametags.INSTANCE.getEnabled() && Nametags.INSTANCE.getTargets().get(entity) != null)
            ci.cancel();
    }

}
