package me.zeroeightsix.kami.feature.hidden

import com.mojang.blaze3d.platform.GlStateManager
import me.zero.alpine.event.EventPriority
import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.event.events.DisplaySizeChangedEvent
import me.zeroeightsix.kami.event.events.RenderEvent
import me.zeroeightsix.kami.event.events.RenderHudEvent
import me.zeroeightsix.kami.event.events.TickEvent
import me.zeroeightsix.kami.feature.FindFeature
import me.zeroeightsix.kami.feature.FullFeature
import me.zeroeightsix.kami.gui.KamiGuiScreen
import me.zeroeightsix.kami.gui.KamiHud.renderHud
import me.zeroeightsix.kami.gui.windows.Settings
import me.zeroeightsix.kami.gui.windows.Settings.rainbowBrightness
import me.zeroeightsix.kami.gui.windows.Settings.rainbowSaturation
import me.zeroeightsix.kami.gui.windows.Settings.rainbowSpeed
import me.zeroeightsix.kami.mc
import me.zeroeightsix.kami.util.KamiTessellator
import me.zeroeightsix.kami.util.Wrapper
import org.lwjgl.opengl.GL11
import java.awt.Color

@FindFeature
object PrepHandler : FullFeature(hidden = true, _alwaysListening = true) {

    private var displayWidth = 0
    private var displayHeight = 0

    @EventHandler
    private val clientTickListener = Listener(EventHook<TickEvent.Client.InGame> { update() } )

    @EventHandler
    private val clientTickListener2 = Listener(EventHook<TickEvent.Client.OutOfGame> { update() } )

    private fun update() {
        if (mc.window.width != displayWidth || mc.window.height != displayHeight) {
            KamiMod.EVENT_BUS.post(DisplaySizeChangedEvent())
            displayWidth = mc.window.width
            displayHeight = mc.window.height
        }
        val speed = rainbowSpeed
        val hue = System.currentTimeMillis() % (360 * speed) / (360f * speed)
        KamiMod.rainbow = Color.HSBtoRGB(
            hue,
            rainbowSaturation,
            rainbowBrightness
        )
    }

    @EventHandler
    var hudEventListener =
        Listener(
            EventHook<RenderHudEvent> {
                if (Wrapper.getMinecraft().currentScreen !is KamiGuiScreen && (Settings.hudWithDebug || !Wrapper.getMinecraft().options.debugEnabled)) {
                    renderHud()
                }
            }, EventPriority.LOW
        )

    @EventHandler
    private val preWorldListener =
        Listener(
            EventHook<RenderEvent.World> {
                mc.profiler.push("kami")
                mc.profiler.push("setup")
                GlStateManager.disableTexture()
                GlStateManager.enableBlend()
                GlStateManager.disableAlphaTest()
                GlStateManager.blendFuncSeparate(
                    GL11.GL_SRC_ALPHA,
                    GL11.GL_ONE_MINUS_SRC_ALPHA,
                    1,
                    0
                )
                GlStateManager.shadeModel(GL11.GL_SMOOTH)
                GlStateManager.disableDepthTest()
                GlStateManager.lineWidth(1f)
                mc.profiler.pop()
            }, EventPriority.HIGHEST
        )

    @EventHandler
    var postWorldListener =
        Listener(
            EventHook<RenderEvent.World> {
                mc.profiler.push("release")
                GlStateManager.lineWidth(1f)
                GlStateManager.shadeModel(GL11.GL_FLAT)
                GlStateManager.disableBlend()
                GlStateManager.enableAlphaTest()
                GlStateManager.enableTexture()
                GlStateManager.enableDepthTest()
                GlStateManager.enableCull()
                KamiTessellator.releaseGL()
                mc.profiler.pop()
                mc.profiler.pop()
            }, EventPriority.LOWEST
        )

}
