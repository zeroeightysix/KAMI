package me.zeroeightsix.kami.setting.builder.numerical;

import me.zeroeightsix.kami.setting.impl.numerical.FloatSetting;
import me.zeroeightsix.kami.setting.impl.numerical.NumberSetting;

/**
 * Created by 086 on 13/10/2018.
 */
public class FloatSettingBuilder extends NumericalSettingBuilder<Float> {
    @Override
    public NumberSetting build() {
        return new FloatSetting(initialValue, predicate(), consumer(), name, visibilityPredicate(), min, max);
    }
}
