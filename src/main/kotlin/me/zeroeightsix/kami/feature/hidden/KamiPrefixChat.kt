package me.zeroeightsix.kami.feature.hidden

import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.event.CharTypedEvent
import me.zeroeightsix.kami.feature.Feature
import me.zeroeightsix.kami.feature.FindFeature
import me.zeroeightsix.kami.gui.windows.Settings
import me.zeroeightsix.kami.mc
import me.zeroeightsix.kami.mixin.extend.openChatScreen

@FindFeature
object KamiPrefixChat : Feature {
    override var name: String = "Kami prefix chat opener"
    override var hidden: Boolean = true

    override fun initListening() {
        KamiMod.EVENT_BUS.subscribe(Listener({ event: CharTypedEvent ->
            if (Settings.openChatWhenCommandPrefixPressed && mc.currentScreen === null && event.char == Settings.commandPrefix) {
                mc.openChatScreen(event.char.toString())
                event.cancel()
            }
        }))
    }
}