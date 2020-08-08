package me.zeroeightsix.kami.mixin.client;

import me.zeroeightsix.kami.feature.module.render.Brightness;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LightmapTextureManager.class)
public class MixinLightmapTextureManager {

    private boolean nightVision;

    @Redirect(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/GameRenderer;getNightVisionStrength(Lnet/minecraft/entity/LivingEntity;F)F"))
    public float getNightVisionStrength(LivingEntity entity, float tickDelta) {
        return nightVision ? Brightness.getCurrentBrightness() : GameRenderer.getNightVisionStrength(entity, tickDelta);
    }

    @Redirect(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;hasStatusEffect(Lnet/minecraft/entity/effect/StatusEffect;)Z", ordinal = 0))
    public boolean hasStatusEffect(ClientPlayerEntity entity, StatusEffect statusEffect) {
        return (nightVision = Brightness.shouldBeActive()) || entity.hasStatusEffect(statusEffect);
    }
}
