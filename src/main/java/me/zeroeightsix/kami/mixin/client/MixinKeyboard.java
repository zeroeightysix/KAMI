package me.zeroeightsix.kami.mixin.client;

import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.event.BindEvent;
import me.zeroeightsix.kami.event.CharTypedEvent;
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

    @Inject(method = "onChar", at = @At(value = "FIELD", target = "Lnet/minecraft/client/MinecraftClient;currentScreen:Lnet/minecraft/client/gui/screen/Screen;"), cancellable = true)
    public void onOneChar(long window, int i, int j, CallbackInfo ci) {
        boolean consumed = false;
        if (Character.charCount(i) == 1)
            consumed = publishCharEvent((char) i);
        else {
            for (char c : Character.toChars(i)) {
                consumed = consumed || publishCharEvent(c);
            }
        }

        if (consumed)
            ci.cancel();
    }

    private static boolean publishCharEvent(char c) {
        CharTypedEvent event = new CharTypedEvent(c);
        KamiMod.EVENT_BUS.post(event);
        return event.isCancelled();
    }

}
