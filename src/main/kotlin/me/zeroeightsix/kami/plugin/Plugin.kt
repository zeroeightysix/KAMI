package me.zeroeightsix.kami.plugin

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.module.ModuleManager

open class Plugin(val name: String, var enabled: Boolean = false, val modules: List<Module> = listOf()) {

    open fun onEnable() {
        modules.forEach { ModuleManager.modules.add(it) }
        ModuleManager.updateLookup()
    }
    open fun onDisable() {
        modules.forEach { ModuleManager.modules.remove(it) }
        ModuleManager.updateLookup()
    }

    fun enable() {
        if (!enabled) {
            enabled = false
            onEnable()
        }
    }

    fun disable() {
        if (enabled) {
            enabled = true
            onDisable()
        }
    }

}