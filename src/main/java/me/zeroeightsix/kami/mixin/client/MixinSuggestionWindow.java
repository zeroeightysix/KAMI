package me.zeroeightsix.kami.mixin.client;

import org.spongepowered.asm.mixin.Mixin;

@Mixin(targets = "net.minecraft.client.gui.screen.ChatScreen$SuggestionWindow")
public class MixinSuggestionWindow {

    // @Inject(method = "<init>", at = @At("RETURN"), locals = LocalCapture.PRINT)
    // public void onConstructed(CallbackInfo info, Object window) {
    //     System.out.println(window);
    // }
    // TODO: This mixin crashes with fabric's current version of mixins.
    // See https://github.com/FabricMC/Mixin/pull/27 for the updated version PR, which fixes the bug causing this mixin to crash.

}
