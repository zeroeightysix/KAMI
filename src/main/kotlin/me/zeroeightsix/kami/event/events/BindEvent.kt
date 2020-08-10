package me.zeroeightsix.kami.event.events

import me.zeroeightsix.kami.event.KamiEvent
import me.zeroeightsix.kami.mc

class BindEvent(val key: Int, val scancode: Int, i: Int) : KamiEvent() {
    val pressed = i != 0
    val ingame = mc.currentScreen == null
}
