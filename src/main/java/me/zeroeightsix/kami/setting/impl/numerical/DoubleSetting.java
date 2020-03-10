package me.zeroeightsix.kami.setting.impl.numerical;

import imgui.ImGui;
import imgui.InputTextFlag;
import imgui.MutableProperty0;
import me.zeroeightsix.kami.setting.converter.AbstractBoxedNumberConverter;
import me.zeroeightsix.kami.setting.converter.BoxedDoubleConverter;

import java.util.function.BiConsumer;
import java.util.function.Predicate;

/**
 * Created by 086 on 12/10/2018.
 */
public class DoubleSetting extends NumberSetting<Double> {

    private static final BoxedDoubleConverter converter = new BoxedDoubleConverter();

    private final MutableProperty0<Float> mirrorProperty;

    public DoubleSetting(Double value, Predicate<Double> restriction, BiConsumer<Double, Double> consumer, String name, Predicate<Double> visibilityPredicate, Double min, Double max) {
        super(value, restriction, consumer, name, visibilityPredicate, min, max);
        mirrorProperty = new MutableProperty0(value.floatValue());
    }

    @Override
    public AbstractBoxedNumberConverter converter() {
        return converter;
    }

    @Override
    public void drawSettings() {
        if (ImGui.INSTANCE.dragFloat(getName(), mirrorProperty, 1f, 0f, 0f, "%.1f", InputTextFlag.EnterReturnsTrue.i)) {
            if (!setValue(mirrorProperty.get().doubleValue())) {
                mirrorProperty.set(getValue().floatValue());
            }
        }
    }

    @Override
    protected boolean drawSettingsNumber() { return false; }
}
