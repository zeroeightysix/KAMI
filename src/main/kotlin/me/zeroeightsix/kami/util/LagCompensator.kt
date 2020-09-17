package me.zeroeightsix.kami.util

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.Listenable
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.event.PacketEvent.Receive
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket
import net.minecraft.util.math.MathHelper
import java.util.Arrays

object LagCompensator : Listenable {
    private val tickRates = FloatArray(20)
    private var nextIndex = 0
    private var timeLastTimeUpdate: Long = 0

    @EventHandler
    var packetEventListener = Listener(
        { event: Receive ->
            if (event.packet is WorldTimeUpdateS2CPacket) {
                onTimeUpdate()
            }
        }
    )

    fun reset() {
        nextIndex = 0
        timeLastTimeUpdate = -1L
        Arrays.fill(tickRates, 0.0f)
    }

    val tickRate: Float
        get() {
            var numTicks = 0.0f
            var sumTickRates = 0.0f
            for (tickRate in tickRates) {
                if (tickRate > 0.0f) {
                    sumTickRates += tickRate
                    numTicks += 1.0f
                }
            }
            return MathHelper.clamp(sumTickRates / numTicks, 0.0f, 20.0f)
        }

    fun onTimeUpdate() {
        if (timeLastTimeUpdate != -1L) {
            val timeElapsed = (System.currentTimeMillis() - timeLastTimeUpdate).toFloat() / 1000.0f
            tickRates[nextIndex % tickRates.size] = MathHelper.clamp(20.0f / timeElapsed, 0.0f, 20.0f)
            nextIndex += 1
        }
        timeLastTimeUpdate = System.currentTimeMillis()
    }

    init {
        KamiMod.EVENT_BUS.subscribe(this)
        reset()
    }
}
