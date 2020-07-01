package me.zeroeightsix.kami.mixin.client;

import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.event.events.RenderBossBarEvent;
import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

@Mixin(BossBarHud.class)
public class MixinBossBarHud {

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Ljava/util/Collection;iterator()Ljava/util/Iterator;"))
    public Iterator<ClientBossBar> onRender(Collection<ClientBossBar> collection) {
        RenderBossBarEvent.GetIterator event = new RenderBossBarEvent.GetIterator(collection.iterator());
        KamiMod.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            return Collections.emptyIterator();
        }
        return event.getIterator();
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/ClientBossBar;getName()Lnet/minecraft/text/Text;"))
    public Text onAsFormattedString(ClientBossBar clientBossBar) {
        RenderBossBarEvent.GetText event = new RenderBossBarEvent.GetText(clientBossBar, clientBossBar.getName());
        KamiMod.EVENT_BUS.post(event);
        return event.getText();
    }

    @ModifyConstant(method = "render", constant = @Constant(intValue = 9, log = true, ordinal = 1))
    public int modifySpacingConstant(int j) {
        RenderBossBarEvent.Spacing spacing = new RenderBossBarEvent.Spacing(j);
        KamiMod.EVENT_BUS.post(spacing);
        return spacing.getSpacing();
    }

}
