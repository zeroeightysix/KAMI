package me.zeroeightsix.kami.setting.converter;

import com.google.common.base.Converter;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

/**
 * Created by 086 on 13/10/2018.
 */
public class BooleanConverter extends Converter<Boolean, JsonElement> {

    @Override
    protected JsonElement doForward(Boolean aBoolean) {
        return new JsonPrimitive(aBoolean);
    }

    @Override
    protected Boolean doBackward(JsonElement s) {
        return s.getAsBoolean();
    }

}
