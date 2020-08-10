package me.zeroeightsix.kami.event.events

import me.zeroeightsix.kami.event.KamiEvent
import net.minecraft.client.render.LightmapTextureManager

class RenderWeatherEvent(
    val manager: LightmapTextureManager,
    val f: Float,
    val d: Double,
    val e: Double,
    val g: Double
) : KamiEvent()
