package me.zeroeightsix.kami.feature

import me.zeroeightsix.kami.util.Bind

interface Listening {

    fun isAlwaysListening(): Boolean
    
    fun getBind(): Bind

}