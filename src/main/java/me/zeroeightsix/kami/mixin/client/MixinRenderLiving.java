package me.zeroeightsix.kami.mixin.client;

import me.zeroeightsix.kami.module.ModuleManager;
import me.zeroeightsix.kami.module.modules.render.Chams;
import net.minecraft.client.render.entity.LivingEntityRenderer;
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
    private void injectChamsPre(LivingEntity entity, float limbAngle, float limbDistance, float age, float headYaw, float headPitch, float scale, CallbackInfo info) {
        if (ModuleManager.isModuleEnabled("Chams") && Chams.renderChams(entity)) {
            GL11.glEnable(32823);
            GL11.glPolygonOffset(1.0f, -1000000.0f);
        }
    }

    @Inject(method = "render", at = @At("RETURN"))
    private <S extends LivingEntity> void injectChamsPost(S entity, float limbAngle, float limbDistance, float age, float headYaw, float headPitch, float scale, CallbackInfo info) {
        if (ModuleManager.isModuleEnabled("Chams") && Chams.renderChams(entity)) {
            GL11.glPolygonOffset(1.0f, 1000000.0f);
            GL11.glDisable(32823);
        }
    }

}
