package me.zeroeightsix.kami.feature

import me.zero.alpine.listener.Listenable
import me.zeroeightsix.kami.util.Bind

interface Listening : Listenable {

    fun isAlwaysListening(): Boolean

    fun getBind(): Bind

}