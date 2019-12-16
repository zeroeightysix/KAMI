package me.zeroeightsix.kami.setting.impl;

import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.converter.CharacterConverter;

import java.util.function.BiConsumer;
import java.util.function.Predicate;

/**
 * Created by 086 on 12/10/2018.
 */
public class CharacterSetting extends Setting<Character> {

    private static final CharacterConverter converter = new CharacterConverter();

    public CharacterSetting(char value, Predicate<Character> restriction, BiConsumer<Character, Character> consumer, String name, Predicate<Character> visibilityPredicate) {
        super(value, restriction, consumer, name, visibilityPredicate);
    }

    @Override
    public CharacterConverter converter() {
        return converter;
    }

}
