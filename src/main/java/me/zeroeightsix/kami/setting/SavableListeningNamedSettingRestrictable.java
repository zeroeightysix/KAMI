package me.zeroeightsix.kami.setting;

import me.zeroeightsix.kami.setting.converter.Convertable;

import java.util.function.BiConsumer;
import java.util.function.Predicate;

/**
 * Created by 086 on 12/10/2018.
 */
public abstract class SavableListeningNamedSettingRestrictable<T> extends ListeningNamedSettingRestrictable<T> implements Convertable {

    public SavableListeningNamedSettingRestrictable(T value, Predicate<T> restriction, BiConsumer<T, T> consumer, String name) {
        super(value, restriction, consumer, name);
    }
}
