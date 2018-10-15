package me.zeroeightsix.kami.setting;

/**
 * Created by 086 on 12/10/2018.
 */
public abstract class Setting<T> implements ISetting<T> {

    T value;

    public Setting(T value) {
        this.value = value;
    }

    @Override
    public T getValue() {
        return value;
    }

    @Override
    public boolean isVisible() {
        return true;
    }

    @Override
    public boolean setValue(T value) {
        this.value = value;
        return true;
    }

}
