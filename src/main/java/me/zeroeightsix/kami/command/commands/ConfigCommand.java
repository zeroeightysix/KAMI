package me.zeroeightsix.kami.command.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.command.KamiCommandSource;
import net.minecraft.server.command.CommandSource;
import net.minecraft.text.LiteralText;

import static me.zeroeightsix.kami.util.Texts.*;
import static net.minecraft.util.Formatting.*;

/**
 * Created by 086 on 14/10/2018.
 */
public class ConfigCommand extends Command {

    private static final DynamicCommandExceptionType FAILED_EXCEPTION = new DynamicCommandExceptionType(o -> new LiteralText(o.toString()));

    @Override
    public void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(
            LiteralArgumentBuilder.<CommandSource>literal("config")
                    .then(LiteralArgumentBuilder.<CommandSource>literal("reload")
                            .executes(context -> {
                                KamiMod.loadConfiguration();
                                ((KamiCommandSource) context.getSource()).sendFeedback(
                                        f(GOLD, append(
                                                lit("Reloaded configuration "),
                                                flit(YELLOW, KamiMod.getConfigName()),
                                                lit("!")
                                        ))
                                );
                                return 0;
                            })
                    )
                    .then(LiteralArgumentBuilder.<CommandSource>literal("save")
                            .executes(context -> {
                                KamiMod.saveConfiguration();
                                ((KamiCommandSource) context.getSource()).sendFeedback(
                                        f(GOLD, append(
                                                lit("Saved configuration "),
                                                flit(YELLOW, KamiMod.getConfigName()),
                                                lit("!")
                                        ))
                                );
                                return 0;
                            })
                    )
                    .then(LiteralArgumentBuilder.<CommandSource>literal("switch")
                            .then(RequiredArgumentBuilder.<CommandSource, String>argument("filename", StringArgumentType.string())
                                .executes(context -> {
                                    String filename = context.getArgument("filename", String.class);
                                    if (!KamiMod.isFilenameValid(filename)) {
                                        throw FAILED_EXCEPTION.create("Invalid filename '" + filename + "'!");
                                    }
                                    KamiCommandSource source = (KamiCommandSource) context.getSource();
                                    KamiMod.saveConfiguration();
                                    source.sendFeedback(f(GOLD, append(
                                            lit("Saved "),
                                            flit(YELLOW, KamiMod.getConfigName())
                                    )));
                                    KamiMod.setLastConfigName(filename);
                                    source.sendFeedback(f(GOLD, append(
                                            lit("Set "),
                                            flit(YELLOW, "KAMILastConfig.txt"),
                                            lit(" to "),
                                            flit(LIGHT_PURPLE, filename)
                                    )));
                                    KamiMod.loadConfiguration();
                                    source.sendFeedback(f(GOLD, append(
                                            lit("Loaded "),
                                            flit(YELLOW, filename),
                                            lit("!")
                                    )));
                                    return 0;
                                })
                            )
                    )
        );
    }

}
