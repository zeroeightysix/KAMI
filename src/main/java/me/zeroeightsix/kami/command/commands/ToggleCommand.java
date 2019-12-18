package me.zeroeightsix.kami.command.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.command.KamiCommandSource;
import me.zeroeightsix.kami.command.ModuleArgumentType;
import me.zeroeightsix.kami.module.Module;
import net.minecraft.server.command.CommandSource;

import static net.minecraft.util.Formatting.*;
import static me.zeroeightsix.kami.util.Texts.*;

/**
 * Created by 086 on 17/11/2017.
 */
public class ToggleCommand extends Command {

    public static boolean modifiersEnabled = true; // TODO: move this somewhere appropriate

    @Override
    public void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(
            LiteralArgumentBuilder.<CommandSource>literal("toggle")
                .then(
                    RequiredArgumentBuilder.<CommandSource, Module>argument("module", ModuleArgumentType.module())
                        .executes(context -> {
                            Module m = context.getArgument("module", Module.class);
                            m.toggle();
                            ((KamiCommandSource) context.getSource()).sendFeedback(
                                    f(GOLD, append(
                                            lit("Toggled module "),
                                            flit(YELLOW, m.getName()),
                                            lit(", now "),
                                            flit(m.isEnabled() ? GREEN : RED, m.isEnabled() ? "ON" : "OFF")
                                    ))
                            );
                            return 0;
                        })
                )
        );
    }
}
