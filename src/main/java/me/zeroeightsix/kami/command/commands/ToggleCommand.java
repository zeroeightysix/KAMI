package me.zeroeightsix.kami.command.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.command.KamiCommandSource;
import me.zeroeightsix.kami.command.ModuleArgumentType;
import me.zeroeightsix.kami.module.Module;
import net.minecraft.server.command.CommandSource;
import net.minecraft.text.LiteralText;

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
                                    new LiteralText((m.isEnabled() ? "Enabled" : "Disabled") + " " + m.getName() + "."));
                            return 0;
                        })
                )
        );
    }
}
