package me.zeroeightsix.kami.feature

open class SimpleFeature(_enabled: Boolean = false, hidden: Boolean) : AbstractFeature(hidden) {

    var enabled = _enabled

    override fun enable(): Boolean {
        return isDisabled().then {
            enabled = true
            onEnable()
            true
        } ?: false
    }

    override fun disable(): Boolean {
        return isEnabled().then {
            enabled = false
            onDisable()
            true
        } ?: false
    }

    override fun isEnabled(): Boolean {
        return enabled
    }

    override fun isDisabled(): Boolean {
        return !isEnabled()
    }


}