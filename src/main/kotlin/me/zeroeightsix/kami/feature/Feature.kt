package me.zeroeightsix.kami.feature

interface Feature {

    var name: String
    var hidden: Boolean

    /**
     * Called once when the module is initialised.
     *
     * Preferred over running code in the constructor (or kotlin `init`, which is the same) as it assures all fields are initialised by the JVM.
     */
    fun init() {}
}