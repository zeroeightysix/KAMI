package me.zeroeightsix.kami.setting.converter;

import com.google.common.base.Converter;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

/**
 * Created by 086 on 13/10/2018.
 */
public class StringConverter extends Converter<String, JsonElement> {
    @Override
    protected JsonElement doForward(String s) {
        return new JsonPrimitive(s);
    }

    @Override
    protected String doBackward(JsonElement s) {
        return s.getAsString();
    }
}
