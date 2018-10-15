package me.zeroeightsix.kami.setting.builder.numerical;

import me.zeroeightsix.kami.setting.impl.numerical.IntegerSetting;
import me.zeroeightsix.kami.setting.impl.numerical.NumberSetting;

/**
 * Created by 086 on 13/10/2018.
 */
public class IntegerSettingBuilder extends NumericalSettingBuilder<Integer> {
    @Override
    public NumberSetting build() {
        return new IntegerSetting(initialValue, predicate(), consumer(), name, visibilityPredicate(), min, max);
    }
}
