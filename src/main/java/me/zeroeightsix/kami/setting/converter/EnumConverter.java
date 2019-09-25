package me.zeroeightsix.kami.setting.converter;

import com.google.common.base.Converter;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

/**
 * Created by 086 on 14/10/2018.
 */
public class EnumConverter extends Converter<Enum, JsonElement> {

    Class<? extends Enum> clazz;

    public EnumConverter(Class<? extends Enum> clazz) {
        this.clazz = clazz;
    }

    @Override
    protected JsonElement doForward(Enum anEnum) {
        return new JsonPrimitive(anEnum.toString());
    }

    @Override
    protected Enum doBackward(JsonElement jsonElement) {
        return Enum.valueOf(clazz, jsonElement.getAsString());
    }
}
