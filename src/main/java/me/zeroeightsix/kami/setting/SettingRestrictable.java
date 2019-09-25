package me.zeroeightsix.kami.setting;

import java.util.function.Predicate;

/**
 * Created by 086 on 12/10/2018.
 */
public abstract class SettingRestrictable<T> extends Setting<T> {

    /**
     * Returns false if the value is "out of bounds"
     */
    private Predicate<T> restriction;

    public SettingRestrictable(T value, Predicate<T> restriction) {
        super(value);
        this.restriction = restriction;
    }

    @Override
    public boolean setValue(T value) {
        return restriction.test(value) && super.setValue(value);
    }

}
