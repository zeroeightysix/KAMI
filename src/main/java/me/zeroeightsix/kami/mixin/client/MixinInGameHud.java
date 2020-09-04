package me.zeroeightsix.kami.mixin.client;

import com.mojang.blaze3d.systems.RenderSystem;
import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.event.RenderGuiEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class MixinInGameHud {

    // This inject targets a point after vignette is rendered, but before any other GUI element.
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;getArmorStack(I)Lnet/minecraft/item/ItemStack;"))
    public void onGetArmorStack(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        RenderGuiEvent event = new RenderGuiEvent(MinecraftClient.getInstance().getWindow(), matrices);
        KamiMod.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            ci.cancel();
        } else {
            // Listeners for RenderGuiEvent draw text.
            // It looks like drawing text disables blend sometimes,
            // making the hotbar (first thing rendered after this), look opaque.
            // We make sure it's enabled back here.
            RenderSystem.enableBlend();
        }
    }

}
