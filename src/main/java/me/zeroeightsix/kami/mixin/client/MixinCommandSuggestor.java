package me.zeroeightsix.kami.mixin.client;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.suggestion.Suggestions;
import me.zeroeightsix.kami.feature.command.Command;
import me.zeroeightsix.kami.gui.windows.Settings;
import me.zeroeightsix.kami.util.Wrapper;
import net.minecraft.client.gui.screen.CommandSuggestor;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.command.CommandSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.concurrent.CompletableFuture;

@Mixin(CommandSuggestor.class)
public abstract class MixinCommandSuggestor {

    @Shadow
    @Final
    private boolean slashRequired;

    @Shadow
    private ParseResults<CommandSource> parse;

    @Shadow
    @Final
    private TextFieldWidget textField;

    @Shadow
    private boolean completingSuggestions;

    @Shadow
    private CompletableFuture<Suggestions> pendingSuggestions;

    @Shadow
    public abstract void show();

    @Inject(method = "refresh",
            at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/StringReader;canRead()Z"),
            cancellable = true,
            locals = LocalCapture.CAPTURE_FAILHARD)
    public void refresh(CallbackInfo ci, String string, StringReader stringReader) {
        if (slashRequired) return; // Command block

        int i;
        if (stringReader.canRead() && stringReader.peek() == Settings.INSTANCE.getCommandPrefix()) {
            stringReader.skip();
            CommandDispatcher<CommandSource> commandDispatcher = Command.dispatcher;
            if (this.parse == null) {
                this.parse = commandDispatcher.parse(stringReader, Wrapper.getPlayer().networkHandler.getCommandSource());
            }

            i = this.textField.getCursor();
            if (i >= 1 && (!this.completingSuggestions)) {
                this.pendingSuggestions = commandDispatcher.getCompletionSuggestions(this.parse, i);
                this.pendingSuggestions.thenRun(() -> {
                    if (this.pendingSuggestions.isDone()) {
                        this.show();
                    }
                });
            }

            ci.cancel();
        }
    }

}
