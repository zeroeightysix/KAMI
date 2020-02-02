package me.zeroeightsix.kami.plugin

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.module.ModuleManager
import me.zeroeightsix.kami.module.ModulePlay

open class Plugin(name: String, description: String, val modules: List<ModulePlay> = listOf()):
    Module(name, description) {

    override fun onEnable() {
        super.onEnable()
        modules.forEach { ModuleManager.modules.add(it) }
        ModuleManager.updateLookup()
    }

    override fun onDisable() {
        super.onDisable()
        modules.forEach { ModuleManager.modules.remove(it) }
        ModuleManager.updateLookup()
    }

}