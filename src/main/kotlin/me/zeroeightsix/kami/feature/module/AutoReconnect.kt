package me.zeroeightsix.kami.feature.module

import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting
import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.ScreenEvent
import me.zeroeightsix.kami.event.ScreenEvent.Displayed
import me.zeroeightsix.kami.mixin.client.IDisconnectedScreen
import net.minecraft.client.gui.screen.ConnectScreen
import net.minecraft.client.gui.screen.DisconnectedScreen
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.network.ServerInfo
import net.minecraft.client.util.math.MatrixStack
import kotlin.math.floor

@Module.Info(
    name = "AutoReconnect",
    description = "Automatically reconnects after being disconnected",
    category = Module.Category.MISC,
    alwaysListening = true
)
object AutoReconnect : Module() {

    private var cServer: ServerInfo? = null

    @Setting
    private var seconds: @Setting.Constrain.Range(
        min = 0.0, /* TODO: Remove when kotlin bug fixed */
        max = java.lang.Double.POSITIVE_INFINITY,
        step = java.lang.Double.MIN_VALUE
    ) Int = 5;

    @EventHandler
    val closedListener =
        Listener(
            { event: ScreenEvent.Closed ->
                if (event.screen is ConnectScreen) cServer =
                    mc.currentServerEntry
            }
        )

    @EventHandler
    val displayedListener = Listener(
        { event: Displayed ->
            if (enabled && event.screen is DisconnectedScreen && event.screen !is KamiDisconnectedScreen && (cServer != null || mc.currentServerEntry != null)) event.screen =
                KamiDisconnectedScreen(event.screen as DisconnectedScreen)
        }
    )

    private class KamiDisconnectedScreen(disconnected: DisconnectedScreen) : DisconnectedScreen(
        (disconnected as IDisconnectedScreen).parent,
        disconnected.title,
        (disconnected as IDisconnectedScreen).reason
    ) {
        private val parent: Screen
        var millis = seconds.toLong() * 1000
        var cTime: Long

        override fun tick() {
            if (millis <= 0) mc.openScreen(
                ConnectScreen(
                    parent,
                    mc,
                    if (cServer == null) mc.currentServerEntry else cServer
                )
            )
        }

        override fun render(matrices: MatrixStack?, mouseX: Int, mouseY: Int, partialTicks: Float) {
            super.render(matrices, mouseX, mouseY, partialTicks)
            val a = System.currentTimeMillis()
            millis -= (a - cTime).toInt()
            cTime = a
            val s = "Reconnecting in " + 0.0.coerceAtLeast(floor(millis.toDouble() / 100) / 10) + "s"
            textRenderer.drawWithShadow(matrices,s, width / 2 - textRenderer.getWidth(s) / 2.toFloat(), height - 16.toFloat(), 0xffffff)
        }

        init {
            cTime = System.currentTimeMillis()
            parent = (disconnected as IDisconnectedScreen).parent
        }

    }
}
