package me.zeroeightsix.kami.feature.plugin

import me.zeroeightsix.kami.feature.AbstractFeature
import me.zeroeightsix.kami.feature.FeatureManager
import me.zeroeightsix.kami.feature.module.Module

open class Plugin(name: String, description: String, val modules: List<Module> = listOf()):
    AbstractFeature(name, description) {

    override fun onEnable() {
        super.onEnable()
        modules.forEach { FeatureManager.addFeature(it) }
    }

    override fun onDisable() {
        super.onDisable()
        modules.forEach { FeatureManager.removeFeature(it) }
    }

}