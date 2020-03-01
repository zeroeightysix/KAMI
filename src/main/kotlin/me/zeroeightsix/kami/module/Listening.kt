package me.zeroeightsix.kami.module

import me.zeroeightsix.kami.util.Bind

interface Listening {

    fun isAlwaysListening(): Boolean
    
    fun getBind(): Bind

}