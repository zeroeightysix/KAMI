package me.zeroeightsix.kami.feature.command

import me.zeroeightsix.kami.util.Wrapper
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientCommandSource
import net.minecraft.client.network.ClientPlayNetworkHandler
import net.minecraft.text.Text

class KamiCommandSource(client: ClientPlayNetworkHandler?, minecraftClient: MinecraftClient?) :
    ClientCommandSource(client, minecraftClient) {
    fun sendFeedback(text: Text?) {
        Wrapper.getPlayer().sendMessage(text,false)
    }
}