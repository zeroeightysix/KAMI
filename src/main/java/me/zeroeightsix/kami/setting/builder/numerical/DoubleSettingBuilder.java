package me.zeroeightsix.kami.setting.builder.numerical;

import me.zeroeightsix.kami.setting.impl.numerical.DoubleSetting;
import me.zeroeightsix.kami.setting.impl.numerical.NumberSetting;

/**
 * Created by 086 on 13/10/2018.
 */
public class DoubleSettingBuilder extends NumericalSettingBuilder<Double> {
    @Override
    public NumberSetting build() {
        return new DoubleSetting(initialValue, predicate(), consumer(), name, visibilityPredicate(), min, max);
    }
}
