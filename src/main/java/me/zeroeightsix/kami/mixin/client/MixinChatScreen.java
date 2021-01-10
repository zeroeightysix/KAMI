package me.zeroeightsix.kami.mixin.client;

import me.zeroeightsix.kami.gui.windows.Settings;
import me.zeroeightsix.kami.util.Wrapper;
import me.zeroeightsix.kami.util.text.MessageHelper;
import net.minecraft.client.gui.screen.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ChatScreen.class)
public abstract class MixinChatScreen {

    @Redirect(method = "keyPressed", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ChatScreen;sendMessage(Ljava/lang/String;)V"))
    public void sendMessage(ChatScreen screen, String message) {
        if (message.startsWith(String.valueOf(Settings.INSTANCE.getCommandPrefix()))) {
            Wrapper.getMinecraft().inGameHud.getChatHud().addToMessageHistory(message);

            MessageHelper.INSTANCE.executeKamiCommand(message, true);
        } else {
            screen.sendMessage(message);
        }
    }

}
