package me.zeroeightsix.kami.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import me.zeroeightsix.kami.setting.Setting;
import net.minecraft.text.LiteralText;

import java.util.concurrent.CompletableFuture;

public class SettingValueArgumentType extends DependantArgumentType<String, Setting> {

    public static final DynamicCommandExceptionType INVALID_VALUE_EXCEPTION = new DynamicCommandExceptionType((object) -> new LiteralText("Invalid value '" + ((Object[]) object)[0] + "' for property '" + ((Object[]) object)[1] + "'"));

    public SettingValueArgumentType(ArgumentType<Setting> dependantType, String dependantArgument, int shift) {
        super(dependantType, dependantArgument, shift);
    }

    public static SettingValueArgumentType value(ArgumentType<Setting> dependantType, String dependantArgument, int shift) {
        return new SettingValueArgumentType(dependantType, dependantArgument, shift);
    }

    @Override
    public String parse(StringReader reader) throws CommandSyntaxException {
        Setting setting = findDependencyValue(reader);
        String string = reader.readUnquotedString();
        try {
            Object v = setting.convertFromString(string);
            return string;
        } catch (Exception ignored) {}
        throw INVALID_VALUE_EXCEPTION.create(new Object[] { string, setting.getName() });
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return findDependencyValue(context, Setting.class).listSuggestions(context, builder);
    }
}
