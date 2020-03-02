package me.zeroeightsix.kami.feature.plugin

import me.zeroeightsix.kami.feature.AbstractFeature
import me.zeroeightsix.kami.feature.FeatureManager
import me.zeroeightsix.kami.feature.FullFeature

open class Plugin(name: String,
                  description: String,
                  hidden: Boolean = false,
                  alwaysListening: Boolean = true,
                  val features: List<AbstractFeature> = listOf()):
    FullFeature(name, description, hidden, alwaysListening) {

    override fun onEnable() {
        super.onEnable()
        features.forEach { FeatureManager.addFeature(it) }
    }

    override fun onDisable() {
        super.onDisable()
        features.forEach { FeatureManager.removeFeature(it) }
    }

}