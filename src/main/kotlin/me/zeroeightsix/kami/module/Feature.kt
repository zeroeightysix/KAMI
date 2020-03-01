package me.zeroeightsix.kami.module

import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.setting.builder.SettingBuilder

open class Feature(private val originalName: String = "No name", var description: String = "No description") {

    var settingList = mutableListOf<Setting<*>>()
    var enabled: Setting<Boolean> = register(
        Settings.booleanBuilder("Enabled").withVisibility { false }.withValue(false).withConsumer { old, new ->
            if (old != new) {
                if (new) onEnable()
                else onDisable()
            }
        }.build()
    )
    val name = register(
        Settings.stringBuilder("Name").withValue(originalName).withRestriction { it.isNotEmpty() }.build()
    )

    operator fun Setting<Boolean>.not() = !value

    /**
     * @return Whether or not the module was enabled
     */
    fun enable(): Boolean {
        if (enabled.value) return false
        enabled.value = true
        return true
    }

    /**
     * @return Whether or not the module was disabled
     */
    fun disable(): Boolean {
        if (!enabled) return false
        enabled.value = false
        return true
    }

    open fun toggle() {
        enabled.value = !enabled
    }

    open fun onEnable() {}
    open fun onDisable() {}

    protected fun <T> register(setting: Setting<T>): Setting<T> {
        settingList.add(setting)
        return SettingBuilder.register<T>(setting, "modules.$originalName")
    }

    fun isDisabled(): Boolean {
        return !enabled
    }

    fun isEnabled(): Boolean {
        return enabled.value
    }

}