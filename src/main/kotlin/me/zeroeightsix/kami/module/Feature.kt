package me.zeroeightsix.kami.module

import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.setting.builder.SettingBuilder

open class Feature(private val originalName: String = "No name", var description: String = "No description") {

    var enabled: Setting<Boolean> = register(
        Settings.booleanBuilder("Enabled").withVisibility { false }.withValue(false).build()
    )
    var settingList = mutableListOf<Setting<*>>()
    val name = register(
        Settings.stringBuilder("Name").withValue(originalName).withRestriction { it.isNotEmpty() }.build()
    )

    operator fun Setting<Boolean>.not() = !value

    /**
     * @return Whether or not the module was enabled
     */
    open fun enable(): Boolean {
        if (!enabled) {
            enabled.value = true
            onEnable()
            return true
        }
        return false
    }

    /**
     * @return Whether or not the module was disabled
     */
    open fun disable(): Boolean {
        if (enabled.value) {
            enabled.value = false
            onDisable()
            return true
        }
        return false
    }

    open fun onEnable() {}
    open fun onDisable() {}

    protected fun <T> register(setting: Setting<T>): Setting<T> {
        if (settingList == null) settingList = mutableListOf()
        settingList.add(setting)
        return SettingBuilder.register<T>(setting, "modules.$originalName")
    }

}