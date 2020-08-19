package me.zeroeightsix.kami.feature

interface Feature {

    var name: String
    var hidden: Boolean

    fun initListening() {}

}
