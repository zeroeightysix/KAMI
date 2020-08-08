package me.zeroeightsix.kami.mixin.client;

import me.zeroeightsix.kami.feature.module.Chams;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author 086
 */
@Mixin(LivingEntityRenderer.class)
public class MixinRenderLiving {

    @Inject(method = "render", at = @At("HEAD"))
    private <T extends LivingEntity> void injectChamsPre(T livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        if (Chams.INSTANCE.isEnabled() && Chams.INSTANCE.renderChams(livingEntity)) {
            GL11.glEnable(32823);
            GL11.glPolygonOffset(1.0f, -1000000.0f);
        }
    }

    @Inject(method = "render", at = @At("RETURN"))
    private <T extends LivingEntity> void injectChamsPost(T livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        if (Chams.INSTANCE.isEnabled() && Chams.INSTANCE.renderChams(livingEntity)) {
            GL11.glPolygonOffset(1.0f, 1000000.0f);
            GL11.glDisable(32823);
        }
    }

}
