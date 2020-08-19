package me.zeroeightsix.kami.mixin.client;

import me.zeroeightsix.kami.feature.ExtraTab;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * Created by 086 on 8/04/2018.
 */
@Mixin(PlayerListHud.class)
public class MixinPlayerListHud {

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Ljava/util/List;subList(II)Ljava/util/List;"))
    public <E> List<E> subList(List<E> list, int fromIndex, int toIndex) {
        return list.subList(fromIndex, ExtraTab.INSTANCE.getEnabled() ? Math.min(ExtraTab.tabSize, list.size()) : toIndex);
    }

    @Inject(method = "getPlayerName", at = @At("HEAD"), cancellable = true)
    public void getPlayerName(PlayerListEntry networkPlayerInfoIn, CallbackInfoReturnable<Text> returnable) {
        if (ExtraTab.INSTANCE.getEnabled()) {
            returnable.cancel();
            returnable.setReturnValue(ExtraTab.getPlayerName(networkPlayerInfoIn));
        }
    }

}
