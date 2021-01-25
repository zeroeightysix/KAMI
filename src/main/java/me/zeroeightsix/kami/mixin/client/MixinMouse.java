package me.zeroeightsix.kami.mixin.client;

import me.zeroeightsix.kami.gui.KamiHud;
import me.zeroeightsix.kami.gui.KamiImgui;
import me.zeroeightsix.kami.util.Wrapper;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MixinMouse {

    @Inject(method = "onMouseScroll", at = @At("HEAD"))
    private void onMouseScroll(long window, double d, double e, CallbackInfo info) {
        if (window == Wrapper.getMinecraft().getWindow().getHandle()) {
            KamiImgui.INSTANCE.mouseScroll(d, e);
        }
    }

}
