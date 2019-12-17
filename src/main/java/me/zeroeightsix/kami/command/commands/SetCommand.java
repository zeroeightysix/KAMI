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

/**
 * Created by 086 on 18/11/2017.
 */
public class SetCommand extends Command {

    private static final DynamicCommandExceptionType FAILED_EXCEPTION = new DynamicCommandExceptionType(o -> new LiteralText(o.toString()));

    @Override
    public void register(CommandDispatcher<CommandSource> dispatcher) {
        ModuleArgumentType moduleArgumentType = ModuleArgumentType.module();
        SettingArgumentType settingArgumentType = SettingArgumentType.setting(moduleArgumentType, "module", 1);
        dispatcher.register(
                LiteralArgumentBuilder.<CommandSource>literal("set")
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
                                    ((KamiCommandSource) context.getSource()).sendFeedback(new LiteralText("Set property '" + setting.getName() + "' of module '" + module.getName() + "' to " + value.toString() + "!"));
                                    return 0;
                                })
                            )
                        )
                    )
        );
    }
}
