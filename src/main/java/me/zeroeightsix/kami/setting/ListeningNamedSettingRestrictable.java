package me.zeroeightsix.kami.setting;

import java.util.function.BiConsumer;
import java.util.function.Predicate;

/**
 * Created by 086 on 12/10/2018.
 */
public abstract class ListeningNamedSettingRestrictable<T> extends ListeningSettingRestrictable<T> implements Named {

    String name;

    public ListeningNamedSettingRestrictable(T value, Predicate<T> restriction, BiConsumer<T, T> consumer, String name) {
        super(value, restriction, consumer);
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

}
