package me.zeroeightsix.kami.setting.converter;

import com.google.gson.JsonElement;

/**
 * Created by 086 on 13/10/2018.
 */
public class BoxedIntegerConverter extends AbstractBoxedNumberConverter<Integer> {
    @Override
    protected Integer doBackward(JsonElement s) {
        return s.getAsInt();
    }
}
