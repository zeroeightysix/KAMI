package me.zeroeightsix.kami.mixin.client;

import me.zeroeightsix.kami.Colour;
import me.zeroeightsix.kami.feature.module.ItemHighlight;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HandledScreen.class)
public abstract class MixinHandledScreen extends DrawableHelper {
    @Inject(method = "drawSlot", at = @At("HEAD"))
    private void onDrawSlot(MatrixStack matrices, Slot slot, CallbackInfo info) {
        if (ItemHighlight.INSTANCE.getEnabled()) {
            Colour c = ItemHighlight.INSTANCE.getHighlightedItems().get(slot.getStack().getItem());
            if (c != null) {
                fill(matrices, slot.x, slot.y, slot.x + 16, slot.y + 16, c.asARGB());
            }
        }
    }
}
