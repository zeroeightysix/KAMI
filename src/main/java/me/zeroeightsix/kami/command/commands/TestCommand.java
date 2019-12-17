package me.zeroeightsix.kami.command.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.command.KamiCommandSource;
import net.minecraft.server.command.CommandSource;
import net.minecraft.text.LiteralText;

public class TestCommand extends Command {

    public static boolean modifiersEnabled = true;

    @Override
    public void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(
                LiteralArgumentBuilder.<CommandSource>literal("test")
                .executes(context -> {
                    KamiCommandSource source = ((KamiCommandSource) context.getSource());
                    source.sendFeedback(new LiteralText("Hello world!"));
                    return 0;
                })
        );
    }

}
