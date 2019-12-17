package me.zeroeightsix.kami.mixin.client;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.command.KamiCommandSource;
import me.zeroeightsix.kami.util.Wrapper;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.server.command.CommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ChatScreen.class)
public abstract class MixinChatScreen {

    @Shadow private ParseResults<CommandSource> parseResults;

    @Shadow protected TextFieldWidget chatField;

    @Inject(method = "updateCommand", at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/StringReader;canRead()Z"), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    public void onUpdateCommand(CallbackInfo info, String string, StringReader stringReader) {
        int i;
        if (stringReader.canRead() && stringReader.peek() == Command.getCommandPrefix()) {
            stringReader.skip();
            CommandDispatcher<CommandSource> commandDispatcher = KamiMod.getInstance().getCommandManager().dispatcher;
            if (this.parseResults == null) {
                this.parseResults = commandDispatcher.parse(stringReader, Wrapper.getPlayer().networkHandler.getCommandSource());
            }

            i = this.chatField.getCursor();
            /*if (i >= 1 && (this.suggestionsWindow == null || !this.completingSuggestion)) {
                this.suggestionsFuture = commandDispatcher.getCompletionSuggestions(this.parseResults, i);
                this.suggestionsFuture.thenRun(() -> {
                    if (this.suggestionsFuture.isDone()) {
                        this.updateCommandFeedback();
                    }
                });
            }*/
            // TODO: see MixinSuggestionWindow

            info.cancel();
        }
    }

    @Redirect(method = "keyPressed", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ChatScreen;sendMessage(Ljava/lang/String;)V"))
    public void sendMessage(ChatScreen screen, String message) {
        if (message.startsWith(String.valueOf(Command.getCommandPrefix()))) {
            Wrapper.getMinecraft().inGameHud.getChatHud().addToMessageHistory(message);

            message = message.substring(1); // cut off command prefix

            try {
                KamiMod.getInstance().getCommandManager().dispatcher.execute(message, new KamiCommandSource(Wrapper.getMinecraft().getNetworkHandler(), Wrapper.getMinecraft()));
            } catch (CommandSyntaxException e) {
                Wrapper.getPlayer().sendMessage(new LiteralText(e.getMessage()).setStyle((new Style()).setColor(Formatting.RED)));
            }
        } else {
            screen.sendMessage(message);
        }
    }

}
