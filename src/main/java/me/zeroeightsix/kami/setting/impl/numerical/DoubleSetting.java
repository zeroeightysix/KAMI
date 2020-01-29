package me.zeroeightsix.kami.setting.impl.numerical;

import imgui.ImGui;
import imgui.InputTextFlag;
import me.zeroeightsix.kami.setting.converter.AbstractBoxedNumberConverter;
import me.zeroeightsix.kami.setting.converter.BoxedDoubleConverter;

import java.util.function.BiConsumer;
import java.util.function.Predicate;

/**
 * Created by 086 on 12/10/2018.
 */
public class DoubleSetting extends NumberSetting<Double> {

    private static final BoxedDoubleConverter converter = new BoxedDoubleConverter();

    public DoubleSetting(Double value, Predicate<Double> restriction, BiConsumer<Double, Double> consumer, String name, Predicate<Double> visibilityPredicate, Double min, Double max) {
        super(value, restriction, consumer, name, visibilityPredicate, min, max);
    }

    @Override
    public AbstractBoxedNumberConverter converter() {
        return converter;
    }

    @Override
    protected boolean drawSettingsNumber() {
        return ImGui.INSTANCE.inputDouble(getName(), property, 1, 10, "%.1f", InputTextFlag.EnterReturnsTrue.i);
    }

}
