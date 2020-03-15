package me.zeroeightsix.kami.event.events

import me.zeroeightsix.kami.event.KamiEvent
import net.minecraft.client.gui.hud.ClientBossBar

class RenderBossBarEvent(val bossBar: ClientBossBar) : KamiEvent()