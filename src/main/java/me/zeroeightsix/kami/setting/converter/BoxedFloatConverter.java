package me.zeroeightsix.kami.setting.converter;

import com.google.gson.JsonElement;

/**
 * Created by 086 on 13/10/2018.
 */
public class BoxedFloatConverter extends AbstractBoxedNumberConverter<Float> {
    @Override
    protected Float doBackward(JsonElement s) {
        return s.getAsFloat();
    }
}
