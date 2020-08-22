package me.zeroeightsix.kami.feature.hidden

import com.mojang.blaze3d.platform.GlStateManager
import me.zero.alpine.event.EventPriority
import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listenable
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.event.DisplaySizeChangedEvent
import me.zeroeightsix.kami.event.RenderEvent
import me.zeroeightsix.kami.event.RenderGuiEvent
import me.zeroeightsix.kami.event.TickEvent
import me.zeroeightsix.kami.feature.Feature
import me.zeroeightsix.kami.feature.FindFeature
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
object PrepHandler : Feature, Listenable {

    override var name = "PrepHandler"
    override var hidden = true

    private var displayWidth = 0
    private var displayHeight = 0

    override fun initListening() {
        KamiMod.EVENT_BUS.subscribe(this)
    }

    @EventHandler
    private val clientTickListener = Listener(EventHook<TickEvent.Client.InGame> { update() })

    @EventHandler
    private val clientTickListener2 = Listener(EventHook<TickEvent.Client.OutOfGame> { update() })

    fun getRainbowHue() = (System.currentTimeMillis() * rainbowSpeed * 0.0005) % 360

    private fun update() {
        if (mc.window.width != displayWidth || mc.window.height != displayHeight) {
            KamiMod.EVENT_BUS.post(DisplaySizeChangedEvent())
            displayWidth = mc.window.width
            displayHeight = mc.window.height
        }
        val hue = getRainbowHue()
        KamiMod.rainbow = Color.HSBtoRGB(
            hue.toFloat(),
            rainbowSaturation,
            rainbowBrightness
        )
    }

    @EventHandler
    var hudEventListener =
        Listener(
            EventHook<RenderGuiEvent> {
                if (Wrapper.getMinecraft().currentScreen !is KamiGuiScreen && (Settings.hudWithDebug || !Wrapper.getMinecraft().options.debugEnabled)) {
                    renderHud(it.matrixStack)
                }
            }, EventPriority.HIGHEST
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
