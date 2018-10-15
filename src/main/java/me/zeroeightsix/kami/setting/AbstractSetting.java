package me.zeroeightsix.kami.setting;

import java.util.function.BiConsumer;
import java.util.function.Predicate;

/**
 * Created by 086 on 12/10/2018.
 */
public abstract class AbstractSetting<T> extends SavableListeningNamedSettingRestrictable<T> {

    private Predicate<T> visibilityPredicate;

    public AbstractSetting(T value, Predicate<T> restriction, BiConsumer<T, T> consumer, String name, Predicate<T> visibilityPredicate) {
        super(value, restriction, consumer, name);
        this.visibilityPredicate = visibilityPredicate;
    }

    @Override
    public boolean isVisible() {
        return visibilityPredicate.test(getValue());
    }

}
