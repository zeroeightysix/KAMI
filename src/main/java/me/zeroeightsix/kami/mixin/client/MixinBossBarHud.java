package me.zeroeightsix.kami.mixin.client;

import net.minecraft.client.gui.hud.BossBarHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(BossBarHud.class)
public class MixinBossBarHud {

    /*@Inject(method = "render", at = @At(value = "INVOKE", target = "Ljava/util/Iterator;next()Ljava/lang/Object;"), cancellable = true)
    public void onRender(CallbackInfo info, ClientBossBar clientBossBar) {
        RenderBossBarEvent event = new RenderBossBarEvent(clientBossBar);
        KamiMod.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            info.cancel();
        }
    }*/

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Ljava/util/Iterator;next()Ljava/lang/Object;", shift = At.Shift.AFTER), cancellable = true, locals = LocalCapture.PRINT)
    public void onRender(CallbackInfo info) {
    }


}
