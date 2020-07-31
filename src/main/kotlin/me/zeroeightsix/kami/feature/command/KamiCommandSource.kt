package me.zeroeightsix.kami.feature.command

import me.zeroeightsix.kami.mc
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientCommandSource
import net.minecraft.client.network.ClientPlayNetworkHandler
import net.minecraft.text.Text

class KamiCommandSource(client: ClientPlayNetworkHandler?, minecraftClient: MinecraftClient?) :
    ClientCommandSource(client, minecraftClient) {
    fun sendFeedback(text: Text?) {
        mc.player?.sendMessage(text,false)
    }
}
