package me.zeroeightsix.kami.event.events

import me.zeroeightsix.kami.event.KamiEvent
import net.minecraft.client.input.Input

class InputUpdateEvent(
    val previousState: Input,
    var newState: Input
) : KamiEvent()