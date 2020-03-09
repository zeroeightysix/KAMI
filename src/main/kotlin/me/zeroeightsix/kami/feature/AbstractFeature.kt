package me.zeroeightsix.kami.feature

import me.zeroeightsix.kami.then

abstract class AbstractFeature(val hidden: Boolean = false) {

    /**
     * @return Whether or not the module was enabled
     */
    abstract fun enable(): Boolean

    /**
     * @return Whether or not the module was disabled
     */
    abstract fun disable(): Boolean

    abstract fun isDisabled(): Boolean
    abstract fun isEnabled(): Boolean

    open fun toggle() {
        if (isEnabled()) disable().then { onDisable() }
        else enable().then { onEnable() }
    }

    open fun onEnable() {}
    open fun onDisable() {}

}