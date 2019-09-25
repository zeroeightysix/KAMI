package me.zeroeightsix.kami.setting.converter;

import com.google.common.base.Converter;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

/**
 * Created by 086 on 13/10/2018.
 */
public abstract class AbstractBoxedNumberConverter<T extends Number> extends Converter<T, JsonElement> {

    @Override
    protected JsonElement doForward(T t) {
        return new JsonPrimitive(t);
    }

}
