package me.zeroeightsix.kami.mixin.client;

import net.minecraft.client.gui.hud.PlayerListHud;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Created by 086 on 8/04/2018.
 */
@Mixin(PlayerListHud.class)
public class MixinGuiPlayerTabOverlay {

    /*@Redirect(method = "render", at = @At(value = "INVOKE", target = "Ljava/util/List;subList(II)Ljava/util/List;"))
    public List subList(List list, int fromIndex, int toIndex) {
        return list.subList(fromIndex, ExtraTab.INSTANCE.isEnabled() ? Math.min(ExtraTab.INSTANCE.tabSize.getValue(), list.size()) : toIndex);
    }

    @Inject(method = "getPlayerName", at = @At("HEAD"), cancellable = true)
    public void getPlayerName(PlayerListEntry networkPlayerInfoIn, CallbackInfoReturnable returnable) {
        if (ExtraTab.INSTANCE.isEnabled()) {
            returnable.cancel();
            returnable.setReturnValue(ExtraTab.getPlayerName(networkPlayerInfoIn));
        }
    }*/ //TODO

}
