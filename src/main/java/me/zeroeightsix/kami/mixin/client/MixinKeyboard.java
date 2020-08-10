package me.zeroeightsix.kami.mixin.client;

import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.event.events.BindEvent;
import net.minecraft.client.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public class MixinKeyboard {

    @Inject(method = "onKey", at = @At(value = "RETURN", ordinal = 4), require = 1, cancellable = true)
    public void onKey(long window, int key, int scancode, int i, int j, CallbackInfo info) {
        BindEvent event = new BindEvent(key, scancode, i);
        KamiMod.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            info.cancel();
        }
    }

}
