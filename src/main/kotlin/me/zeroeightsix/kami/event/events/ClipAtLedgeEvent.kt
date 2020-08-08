package me.zeroeightsix.kami.event.events

import me.zeroeightsix.kami.event.KamiEvent
import net.minecraft.entity.player.PlayerEntity

class ClipAtLedgeEvent(val player: PlayerEntity, var clip: Boolean? = null) : KamiEvent()
