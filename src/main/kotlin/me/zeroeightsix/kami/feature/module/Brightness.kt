package me.zeroeightsix.kami.feature.module

import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting
import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting.Constrain
import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.TickEvent.InGame
import kotlin.math.sin

@Module.Info(name = "Brightness", description = "Makes everything brighter!", category = Module.Category.RENDER)
object Brightness : Module() {
    @Setting(name = "Transition length")
    private var seconds: @Constrain.Range(min = 0.0, max = 10.0, step = 0.1) Float = 1f

    @Setting(comment = "The factor by which to multiply the final brightness. A factor of 1 indicates the normal night vision brightness.")
    private var factor: @Constrain.Range(min = 0.1, max = 2.0, step = 0.02) Float = 1f

    private var currentBrightness = 0f

    private var transitionStack: ArrayDeque<Float>? = null
    
    override fun onEnable() {
        val (min, max) = if (transitionStack != null) Pair(currentBrightness, 1f) else Pair(0f, 1f)
        transitionStack = createTransition((seconds * 20).toInt(), true, min, max)
        super.onEnable()
    }

    override fun onDisable() {
        val (min, max) = if (transitionStack != null) Pair(0f, currentBrightness) else Pair(0f, 1f)
        transitionStack = createTransition((seconds * 20).toInt(), false, min, max)
        alwaysListening = true
        super.onDisable()
    }

    @EventHandler
    private val updateListener = Listener<InGame>(
        {
            transitionStack?.let {
                val b = it.removeLastOrNull()
                // If there are no more entries, the transition has ended.
                // Stop listening, and set the final brightness.
                if (b == null) {
                    alwaysListening = false
                    currentBrightness = if (enabled) 1f else 0f
                    return@Listener
                }
                currentBrightness = b
            }
        })

    private fun createTransition(length: Int, upwards: Boolean, min: Float = 0f, max: Float = 1f): ArrayDeque<Float> {
        val queue = ArrayDeque<Float>(length)
        val fl = length.toFloat()
        val height = max - min
        (0 until length).forEach {
            var v = min + sine(it.toFloat() / fl) * height
            if (upwards) v = max - v
            queue.add(v)
        }
        return queue
    }

    private fun sine(x: Float): Float { // (sin(pi*x-(pi/2)) + 1) / 2
        return (sin(Math.PI * x - Math.PI / 2).toFloat() + 1) / 2
    }

    @JvmStatic
    fun getCurrentBrightness() = currentBrightness * factor

    @JvmStatic
    fun shouldBeActive(): Boolean {
        // != null if in transition
        return enabled || transitionStack != null
    }
}