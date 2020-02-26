package me.zeroeightsix.kami.module

import me.zeroeightsix.kami.KamiMod

open class ListeningFeature(originalName: String = "No name", description: String = "No description", _alwaysListening: Boolean = false) : Feature(originalName, description), Listening {

    var alwaysListening = _alwaysListening
        set(value) {
            field = value
            if (value) KamiMod.EVENT_BUS.subscribe(this)
            else if (!value && isDisabled()) KamiMod.EVENT_BUS.unsubscribe(this)
        }

    override fun enable() = if (super.enable()) {
        if (!alwaysListening) KamiMod.EVENT_BUS.subscribe(this)
        true
    } else false

    override fun disable() = if (super.disable()) {
        if (!alwaysListening) KamiMod.EVENT_BUS.unsubscribe(this)
        true
    } else false

    override fun isAlwaysListening(): Boolean = alwaysListening

}