package me.zeroeightsix.kami.mixin.duck;

import me.zeroeightsix.kami.setting.SettingInterface;

public interface HasSettingInterface<T> {

    SettingInterface<T> getSettingInterface();

    void setSettingInterface(SettingInterface<T> settingInterface);

}
