package me.zeroeightsix.kami.setting.impl.numerical;

import me.zeroeightsix.kami.setting.AbstractSetting;
import me.zeroeightsix.kami.setting.converter.AbstractBoxedNumberConverter;

import java.util.function.BiConsumer;
import java.util.function.Predicate;

/**
 * Created by 086 on 12/10/2018.
 */
public abstract class NumberSetting<T extends Number> extends AbstractSetting<T> {

    private final T min;
    private final T max;

    public NumberSetting(T value, Predicate<T> restriction, BiConsumer<T, T> consumer, String name, Predicate<T> visibilityPredicate, T min, T max) {
        super(value, restriction, consumer, name, visibilityPredicate);
        this.min = min;
        this.max = max;
    }

    /**
     * @return true if this setting has both a defined maximum and minimum
     */
    public boolean isBound() {
        return min != null && max != null;
    }

    @Override
    public abstract AbstractBoxedNumberConverter converter();

    @Override
    public T getValue() {
        return super.getValue();
    }

    public T getMax() {
        return max;
    }

    public T getMin() {
        return min;
    }
}
