package me.zeroeightsix.kami.mixin.client;

import me.zeroeightsix.kami.feature.FeatureManager;
import net.minecraft.client.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public class MixinKeyboard {

    @Inject(method = "onKey", at = @At(value = "RETURN", ordinal = 4), require = 1)
    public void onKey(long window, int key, int scancode, int i, int j, CallbackInfo info) {
        FeatureManager.onBind(key, scancode, i);
    }

}
