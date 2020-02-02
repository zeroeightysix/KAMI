package me.zeroeightsix.kami.plugin

import me.zeroeightsix.kami.module.Feature
import me.zeroeightsix.kami.module.FeatureManager
import me.zeroeightsix.kami.module.Module

open class Plugin(name: String, description: String, val modules: List<Module> = listOf()):
    Feature(name, description) {

    override fun onEnable() {
        super.onEnable()
        modules.forEach { FeatureManager.modules.add(it) }
        FeatureManager.updateLookup()
    }

    override fun onDisable() {
        super.onDisable()
        modules.forEach { FeatureManager.modules.remove(it) }
        FeatureManager.updateLookup()
    }

}