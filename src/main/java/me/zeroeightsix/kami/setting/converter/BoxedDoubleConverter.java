package me.zeroeightsix.kami.setting.converter;

import com.google.gson.JsonElement;

/**
 * Created by 086 on 13/10/2018.
 */
public class BoxedDoubleConverter extends AbstractBoxedNumberConverter<Double> {
    @Override
    protected Double doBackward(JsonElement s) {
        return s.getAsDouble();
    }
}
