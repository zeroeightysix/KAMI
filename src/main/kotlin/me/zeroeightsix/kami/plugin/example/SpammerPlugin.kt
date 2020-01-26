package me.zeroeightsix.kami.plugin.example

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.plugin.Plugin

object SpammerPlugin: Plugin("Spammer", modules = listOf(Spammer)) {
}

@Module.Info(name = "Spammer", description = "Spams the chat", category = Module.Category.MISC)
object Spammer: Module() {
    override fun onUpdate() {
        if (mc.player.age % 100 == 0) {
            mc.player.sendChatMessage("Hello world!")
        }
    }
}