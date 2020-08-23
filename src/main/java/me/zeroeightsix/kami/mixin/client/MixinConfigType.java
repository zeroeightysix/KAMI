package me.zeroeightsix.kami.mixin.client;

import io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.derived.ConfigType;
import me.zeroeightsix.kami.mixin.duck.HasSettingInterface;
import me.zeroeightsix.kami.setting.SettingInterface;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = ConfigType.class, remap = false)
public class MixinConfigType<R> implements HasSettingInterface<R> {
    SettingInterface<R> settingInterface;

    @Override
    public SettingInterface<R> getSettingInterface() {
        return settingInterface;
    }

    @Override
    public void setSettingInterface(SettingInterface<R> settingInterface) {
        this.settingInterface = settingInterface;
    }
}
