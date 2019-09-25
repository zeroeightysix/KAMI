package me.zeroeightsix.kami.setting.converter;

import com.google.common.base.Converter;
import com.google.gson.JsonElement;

/**
 * Created by 086 on 12/10/2018.
 */
public interface Convertable<T> {

    Converter<T, JsonElement> converter();

}
