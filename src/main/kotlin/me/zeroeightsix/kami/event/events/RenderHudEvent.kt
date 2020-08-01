package me.zeroeightsix.kami.event.events

import me.zeroeightsix.kami.event.KamiEvent
import net.minecraft.client.util.Window
import net.minecraft.client.util.math.MatrixStack

class RenderHudEvent(
    val window: Window,
    val matrixStack: MatrixStack
) : KamiEvent()
