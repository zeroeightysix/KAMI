package me.zeroeightsix.kami.command.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import me.zeroeightsix.kami.command.*;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import net.minecraft.server.command.CommandSource;
import net.minecraft.text.LiteralText;

import static net.minecraft.util.Formatting.*;
import static me.zeroeightsix.kami.util.Texts.*;

/**
 * Created by 086 on 18/11/2017.
 */
public class SettingsCommand extends Command {

    private static final DynamicCommandExceptionType FAILED_EXCEPTION = new DynamicCommandExceptionType(o -> new LiteralText(o.toString()));

    @Override
    public void register(CommandDispatcher<CommandSource> dispatcher) {
        ModuleArgumentType moduleArgumentType = ModuleArgumentType.module();
        SettingArgumentType settingArgumentType = SettingArgumentType.setting(moduleArgumentType, "module", 1);
        dispatcher.register(
                LiteralArgumentBuilder.<CommandSource>literal("settings")
                    .then(LiteralArgumentBuilder.<CommandSource>literal("list").then(
                            RequiredArgumentBuilder.<CommandSource, Module>argument("module", ModuleArgumentType.module())
                                .executes(context -> {
                                    KamiCommandSource source = (KamiCommandSource) context.getSource();
                                    Module m = (Module) context.getArgument("module", Module.class);
                                    source.sendFeedback(
                                            i(append(flit(YELLOW, m.getName()), flit(GOLD, " has the following properties:")))
                                    );
                                    m.settingList.forEach(setting -> {
                                        String settingName = setting.getName();
                                        String typeName = setting.getValueClass().getSimpleName();
                                        String value = setting.getValueAsString();
                                        source.sendFeedback(append(
                                                flit(YELLOW, settingName),
                                                lit(" "),
                                                flit(GRAY, "("),
                                                flit(GREEN, typeName),
                                                flit(GRAY, ")"),
                                                flit(WHITE, " = "),
                                                flit(LIGHT_PURPLE, value)
                                        ));
                                    });
                                    return 0;
                                })
                    ))
                    .then(
                            LiteralArgumentBuilder.literal("set")
                                    .then(RequiredArgumentBuilder.<CommandSource, Module>argument("module", moduleArgumentType)
                                            .then(RequiredArgumentBuilder.<CommandSource, Setting>argument("setting", settingArgumentType)
                                                    .then(RequiredArgumentBuilder.<CommandSource, String>argument("value", SettingValueArgumentType.value(settingArgumentType, "setting", 1))
                                                            .executes(context -> {
                                                                Module module = (Module) context.getArgument("module", Module.class);
                                                                Setting setting = (Setting) context.getArgument("setting", Setting.class);
                                                                String stringValue = (String) context.getArgument("value", String.class);
                                                                Object value;
                                                                try {
                                                                    value = setting.convertFromString(stringValue);
                                                                } catch (Exception e) {
                                                                    throw FAILED_EXCEPTION.create("Couldn't convert value from string to setting value (this shouldn't happen)");
                                                                }

                                                                setting.setValue(value);
                                                                ((KamiCommandSource) context.getSource()).sendFeedback(
                                                                        f(GOLD, append(
                                                                                lit("Set property "),
                                                                                flit(YELLOW, setting.getName()),
                                                                                lit(" of module "),
                                                                                flit(YELLOW, module.getName()),
                                                                                lit(" to "),
                                                                                flit(LIGHT_PURPLE, setting.getValueAsString()),
                                                                                lit("!")
                                                                        ))
                                                                );
                                                                return 0;
                                                            })
                                                    )
                                            )
                                    )
                    )
        );
    }
}
