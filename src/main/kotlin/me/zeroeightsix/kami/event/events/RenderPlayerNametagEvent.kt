package me.zeroeightsix.kami.event.events

import me.zeroeightsix.kami.event.KamiEvent
import net.minecraft.client.network.AbstractClientPlayerEntity

//TODO: Generify into RenderTagEvent
class RenderPlayerNametagEvent(val entity: AbstractClientPlayerEntity) : KamiEvent()