package me.zeroeightsix.kami.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import me.zeroeightsix.kami.feature.FeatureManager;
import me.zeroeightsix.kami.module.Module;
import net.minecraft.server.command.CommandSource;
import net.minecraft.text.LiteralText;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class ModuleArgumentType implements ArgumentType<Module> {

    private static final Collection<String> EXAMPLES = Arrays.asList("Aura", "CameraClip", "Flight");
    public static final DynamicCommandExceptionType INVALID_MODULE_EXCEPTION = new DynamicCommandExceptionType((object) -> {
        return new LiteralText("Unknown module '" + object + "'");
    });

    public static ModuleArgumentType module() {
        return new ModuleArgumentType();
    }

    @Override
    public Module parse(StringReader reader) throws CommandSyntaxException {
        String string = reader.readUnquotedString();
        Optional<Module> module = FeatureManager.INSTANCE.getFeatures()
                .stream()
                .filter(feature -> feature instanceof Module)
                .map(feature -> (Module) feature)
                .filter(module1 -> module1.getName().getValue().equals(string))
                .findAny();
        if (!module.isPresent()) {
            throw INVALID_MODULE_EXCEPTION.create(string);
        }
        return module.get();
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(FeatureManager.INSTANCE.getFeatures().stream().map(m -> m.getName().getValue()), builder);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

}
