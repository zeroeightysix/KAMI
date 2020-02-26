package me.zeroeightsix.kami.module

import me.zeroeightsix.kami.KamiMod

open class ListeningFeature(originalName: String = "No name", description: String = "No description", _alwaysListening: Boolean = false) : Feature(originalName, description), Listening {

    var alwaysListening = _alwaysListening
        set(value) {
            field = value
            if (value) KamiMod.EVENT_BUS.subscribe(this)
            else if (!value && isDisabled()) KamiMod.EVENT_BUS.unsubscribe(this)
        }

    override fun onEnable() {
        if (!alwaysListening) KamiMod.EVENT_BUS.subscribe(this)
    }

    override fun onDisable() {
        if (!alwaysListening) KamiMod.EVENT_BUS.unsubscribe(this)
    }

    override fun isAlwaysListening(): Boolean = alwaysListening

}