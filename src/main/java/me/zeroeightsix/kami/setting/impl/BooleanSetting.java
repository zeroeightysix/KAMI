package me.zeroeightsix.kami.setting.impl;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import imgui.ImGui;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.converter.BooleanConverter;
import net.minecraft.server.command.CommandSource;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

/**
 * Created by 086 on 12/10/2018.
 */
public class BooleanSetting extends Setting<Boolean> {

    private boolean[] checkbox;

    private static final BooleanConverter converter = new BooleanConverter();

    public BooleanSetting(Boolean value, Predicate<Boolean> restriction, BiConsumer<Boolean, Boolean> consumer, String name, Predicate<Boolean> visibilityPredicate) {
        super(value, restriction, consumer, name, visibilityPredicate);
        checkbox = new boolean[] { value };
    }

    @Override
    public BooleanConverter converter() {
        return converter;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(new String[] { "true", "false" }, builder);
    }

    @Override
    public void drawSettings() {
        // The checkbox was CLICKED (one time action) and we couldn't set this setting to the checkbox's value
        if (ImGui.INSTANCE.checkbox(getName(), checkbox) && !setValue(checkbox[0])) {
            checkbox[0] = getValue(); // set the checkbox to be equal to the current value
        }
    }
}
