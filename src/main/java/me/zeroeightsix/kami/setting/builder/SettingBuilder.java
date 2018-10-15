package me.zeroeightsix.kami.setting.builder;

import com.google.common.base.MoreObjects;
import me.zeroeightsix.kami.setting.Named;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.SettingsRegister;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

/**
 * Created by 086 on 13/10/2018.
 */
public abstract class SettingBuilder<T> {

    protected String name;
    protected T initialValue;
    protected BiConsumer<T, T> consumer;
    protected List<Predicate<T>> predicateList = new ArrayList<>();
    private Predicate<T> visibilityPredicate;

    public SettingBuilder<T> withValue(T value) {
        this.initialValue = value;
        return this;
    }

    protected Predicate<T> predicate() {
        return predicateList.isEmpty() ? (t) -> true : (t) -> predicateList.stream().allMatch(tPredicate -> tPredicate.test(t));
    }

    protected Predicate<T> visibilityPredicate() {
        return MoreObjects.firstNonNull(visibilityPredicate, t -> true);
    }

    protected BiConsumer<T, T> consumer() {
        return MoreObjects.firstNonNull(consumer, (a, b) -> {});
    }

    public SettingBuilder<T> withConsumer(BiConsumer<T, T> consumer) {
        this.consumer = consumer;
        return this;
    }

    public SettingBuilder<T> withVisibility(Predicate<T> predicate) {
        this.visibilityPredicate = predicate;
        return this;
    }

    public SettingBuilder<T> withName(String name) {
        this.name = name;
        return this;
    }

    public SettingBuilder<T> withRestriction(Predicate<T> predicate) {
        predicateList.add(predicate);
        return this;
    }

    public abstract Setting<T> build();

    public final Setting<T> buildAndRegister(String group) {
        return register(build(), group);
    }

    public static <T> Setting<T> register(Setting<T> setting, String group) {
        if (!(setting instanceof Named)) throw new RuntimeException("Can't register unnamed setting");
        String name = ((Named) setting).getName();
        if (name == null || name.isEmpty()) throw new RuntimeException("Can't register nameless setting");
        SettingsRegister.register(group + "." + name, setting);
        return setting;
    }

}
