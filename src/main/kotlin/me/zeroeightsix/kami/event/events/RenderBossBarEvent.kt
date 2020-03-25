package me.zeroeightsix.kami.event.events

import me.zeroeightsix.kami.event.KamiEvent
import net.minecraft.client.gui.hud.ClientBossBar
import net.minecraft.text.Text

open class RenderBossBarEvent : KamiEvent() {
    class GetIterator(var iterator: Iterator<ClientBossBar>): RenderBossBarEvent()
    class GetText(val bossBar: ClientBossBar, var text: Text): RenderBossBarEvent()
    class Spacing(var spacing: Int): RenderBossBarEvent()
}
