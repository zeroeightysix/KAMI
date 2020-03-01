package me.zeroeightsix.kami.feature.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import me.zeroeightsix.kami.feature.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import net.minecraft.server.command.CommandSource;
import net.minecraft.text.LiteralText;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class SettingArgumentType<T> extends DependantArgumentType<Setting<T>, Module> {

    private static final Collection<String> EXAMPLES = Arrays.asList("enabled", "speed");
    public static final DynamicCommandExceptionType INVALID_SETTING_EXCEPTION = new DynamicCommandExceptionType((object) -> new LiteralText("Unknown setting '" + ((Object[]) object)[0] + "' for module '" + ((Object[]) object)[1]));
    public static final DynamicCommandExceptionType NO_MODULE_EXCEPTION = new DynamicCommandExceptionType((object) -> new LiteralText("No module found"));

    public SettingArgumentType(ArgumentType<Module> dependantType, String dependantArgument, int shift) {
        super(dependantType, dependantArgument, shift);
    }

    public static SettingArgumentType setting(ModuleArgumentType dependentType, String moduleArgName, int shift) {
        return new SettingArgumentType(dependentType, moduleArgName, shift);
    }

    @Override
    public Setting<T> parse(StringReader reader) throws CommandSyntaxException {
        Module module = findDependencyValue(reader);

        if (module == null) {
            throw NO_MODULE_EXCEPTION.create(null);
        }

        String string = reader.readUnquotedString();
        Optional<Setting<?>> s = module.getSettingList().stream().filter(setting -> setting.getName().equalsIgnoreCase(string)).findAny();
        if (s.isPresent()) {
            return (Setting<T>) s.get();
        } else {
            throw INVALID_SETTING_EXCEPTION.create(new Object[] {string, module});
        }
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        Module m = findDependencyValue(context, Module.class);
        if (m != null) {
            return CommandSource.suggestMatching(m.getSettingList().stream().map(Setting::getName), builder);
        } else {
            return null;
        }
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

}
