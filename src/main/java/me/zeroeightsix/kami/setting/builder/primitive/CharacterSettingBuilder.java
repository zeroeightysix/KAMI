package me.zeroeightsix.kami.setting.builder.primitive;

import me.zeroeightsix.kami.setting.builder.SettingBuilder;
import me.zeroeightsix.kami.setting.impl.CharacterSetting;

/**
 * Created by 086 on 13/10/2018.
 */
public class CharacterSettingBuilder extends SettingBuilder<Character> {
    @Override
    public CharacterSetting build() {
        return new CharacterSetting(initialValue, predicate(), consumer(), name, visibilityPredicate());
    }
}
