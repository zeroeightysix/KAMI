package me.zeroeightsix.kami.setting.converter;

import com.google.common.base.Converter;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

/**
 * Created by 086 on 13/10/2018.
 */
public class CharacterConverter extends Converter<Character, JsonElement> {
    @Override
    protected JsonElement doForward(Character c) {
        return new JsonPrimitive(c);
    }

    @Override
    protected Character doBackward(JsonElement s) {
        return s.getAsCharacter();
    }
}
