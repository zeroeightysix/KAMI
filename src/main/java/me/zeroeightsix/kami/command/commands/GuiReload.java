package me.zeroeightsix.kami.command.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.command.KamiCommandSource;
import net.minecraft.server.command.CommandSource;

import static me.zeroeightsix.kami.util.Texts.lit;

public class GuiReload extends Command {
    @Override
    public void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(LiteralArgumentBuilder.<CommandSource>literal("guir").executes(context -> {
            //KamiMod.getInstance().kamiGuiScreen = new KamiGuiScreen();
            KamiMod.getInstance().kamiGuiScreen.reload();
            ((KamiCommandSource) context.getSource()).sendFeedback(lit("you ask i do"));
            return 0;
        }));
    }
}
