package me.zeroeightsix.kami.feature.module

import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting
import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.PacketEvent.Send
import me.zeroeightsix.kami.mixin.client.IChatMessageC2SPacket
import me.zeroeightsix.kami.setting.SettingVisibility
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket

@Module.Info(name = "CustomChat", category = Module.Category.MISC, description = "Modifies your chat messages")
object CustomChat : Module() {
    @Setting(name = "Commands")
    private var commands = false

    // Of course we could omit this setting and just have the default value for the custom suffix be KAMI_SUFFIX.
    // Though, in reality, we _do_ want to promote KAMI - and one way to do that is by making it as easy as possible to fallback to the default one (through this setting).
    @Setting(name = "Custom suffix", comment = "Whether or not to use the default KAMI suffix, or the custom one.")
    private var customSuffix = false

    @Setting(name = "uwuify", comment = "uwuify the chat message")
    private var uwu = false

    @Setting(name = "Suffix")
    @SettingVisibility.Method("ifCustomSuffix")
    private var suffix = " | KAMI"

    fun ifCustomSuffix() = customSuffix
    fun uwuify(t: String): String = t.replace('r', 'w').replace('R', 'W').replace('l', 'w').replace('L', 'W').replace("th", "d").replace("Th", "D").replace("tH", "d").replace("TH", "D")

    private const val KAMI_SUFFIX = " \u23D0 \u1D0B\u1D00\u1D0D\u026A"

    @EventHandler
    var listener = Listener({ event: Send ->
        if (event.packet is ChatMessageC2SPacket) {
            var s = (event.packet as IChatMessageC2SPacket).chatMessage
            if (s.startsWith("/") && !commands) return@Listener
            s = if (uwu) uwuify(s) else s
            s += if (customSuffix) suffix else KAMI_SUFFIX
            if (s.length >= 256) s = s.substring(0, 256)
            (event.packet as IChatMessageC2SPacket).chatMessage = s
        }
    })
}
