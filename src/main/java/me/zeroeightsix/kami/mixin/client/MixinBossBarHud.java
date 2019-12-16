package me.zeroeightsix.kami.mixin.client;

import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.event.events.RenderBossBarEvent;
import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.client.gui.hud.ClientBossBar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;

@Mixin(BossBarHud.class)
public class MixinBossBarHud {

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Ljava/util/Iterator;next()Ljava/lang/Object;", shift = At.Shift.BY, by = 3), cancellable = true)
    public void onRender(CallbackInfo info, int i, int j, Iterator var3, ClientBossBar clientBossBar) {
        RenderBossBarEvent event = new RenderBossBarEvent(clientBossBar);
        KamiMod.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            info.cancel();
        }
    }

}
