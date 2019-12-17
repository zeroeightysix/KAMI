package me.zeroeightsix.kami.setting.impl;

import com.google.common.base.Converter;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.converter.EnumConverter;
import net.minecraft.server.command.CommandSource;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

/**
 * Created by 086 on 14/10/2018.
 */
public class EnumSetting<T extends Enum> extends Setting<T> {

    private EnumConverter converter;
    public final Class<? extends Enum> clazz;

    public EnumSetting(T value, Predicate<T> restriction, BiConsumer<T, T> consumer, String name, Predicate<T> visibilityPredicate, Class<? extends Enum> clazz) {
        super(value, restriction, consumer, name, visibilityPredicate);
        this.converter = new EnumConverter(clazz);
        this.clazz = clazz;
    }

    @Override
    public Converter converter() {
        return converter;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(Arrays.stream(clazz.getEnumConstants()).map(Object::toString), builder);
    }

}
