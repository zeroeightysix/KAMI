package me.zeroeightsix.kami.event

import me.zero.alpine.event.type.Cancellable
import me.zeroeightsix.kami.util.Wrapper

/**
 * Created by 086 on 16/11/2017.
 */
open class KamiEvent : Cancellable() {
    var era = Era.PRE
        protected set
    val partialTicks: Float = Wrapper.getMinecraft().tickDelta

    enum class Era {
        PRE, POST
    }
}