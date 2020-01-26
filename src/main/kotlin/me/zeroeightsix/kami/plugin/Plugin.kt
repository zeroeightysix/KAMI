package me.zeroeightsix.kami.plugin

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.module.ModuleManager

open class Plugin(val name: String, private val _enabled: Boolean = false, val modules: List<Module> = listOf()) {

    var enabled = _enabled
    set(value) {
        if (value && !enabled) {
            field = value
            onEnable()
        } else if (!value && enabled) {
            field = value
            onDisable()
        }
    }

    open fun onEnable() {
        modules.forEach { ModuleManager.modules.add(it) }
        ModuleManager.updateLookup()
    }
    open fun onDisable() {
        modules.forEach { ModuleManager.modules.remove(it) }
        ModuleManager.updateLookup()
    }

    fun enable() {
        enabled = true
    }

    fun disable() {
        enabled = false
    }

}