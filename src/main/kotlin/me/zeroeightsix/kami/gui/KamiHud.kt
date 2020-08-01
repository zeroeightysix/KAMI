package me.zeroeightsix.kami.gui

import glm_.vec2.Vec2
import glm_.vec2.Vec2d
import imgui.ImGui
import imgui.classes.Context
import imgui.classes.IO
import imgui.font.FontConfig
import imgui.impl.gl.ImplGL3
import imgui.impl.glfw.ImplGlfw
import me.zeroeightsix.kami.gui.widgets.EnabledWidgets
import me.zeroeightsix.kami.gui.widgets.PinnableWidget
import me.zeroeightsix.kami.gui.windows.Settings
import me.zeroeightsix.kami.mc
import me.zeroeightsix.kami.util.Wrapper
import net.minecraft.client.util.math.MatrixStack
import uno.glfw.GlfwWindow
import java.util.*

object KamiHud {

    internal var implGl3: ImplGL3
    internal val context: Context
    private val implGlfw: ImplGlfw
    private val io: IO
    private val postDrawStack: Stack<(MatrixStack) -> Unit>

    init {
        val window = GlfwWindow.from(mc.window.handle)
        window.makeContextCurrent()
        context = Context()
        implGlfw = ImplGlfw(window, false, null)
        implGl3 = ImplGL3()
        io = ImGui.io
        io.iniFilename = "kami-imgui.ini"

        val fontCfg = FontConfig()
        fontCfg.oversample put 1
        fontCfg.pixelSnapH = true
        fontCfg.glyphOffset = Vec2(0, -2)
        ImGui.io.fonts.addFontFromFileTTF("assets/kami/Minecraftia.ttf", 12f, fontCfg)
        ImGui.io.fonts.addFontDefault()

        Themes.Variants.values()[Settings.styleIdx].applyStyle()
        ImGui.io.fontDefault = ImGui.io.fonts.fonts[Settings.font]

        postDrawStack = Stack()
    }

    fun renderHud(matrixStack: MatrixStack) {
        frame(matrixStack) {
            if (!EnabledWidgets.hideAll) {
                PinnableWidget.drawFadedBackground = false
                for ((widget, open) in EnabledWidgets.widgets) {
                    if (open.get() && widget.pinned) {
                        widget.showWindow(open, false)
                    }
                }
                PinnableWidget.drawFadedBackground = true
            }
        }
    }

    internal fun frame(matrices: MatrixStack, block: () -> Unit) {
        implGl3.newFrame()
        implGlfw.newFrame()
        ImGui.newFrame()

        try { block() } finally {
            ImGui.render()
            implGl3.renderDrawData(ImGui.drawData!!)
            while (!postDrawStack.isEmpty()) {
                val cmd = postDrawStack.pop()
                cmd?.let {
                    it(matrices)
                }
            }
        }
    }

    fun postDraw(block: (MatrixStack) -> Unit) {
        postDrawStack.push(block)
    }

    fun mouseScroll(d: Double, e: Double) {
        ImplGlfw.scrollCallback(Vec2d(d, e))
    }

    fun getScale(): Int {
        var scale: Int = Wrapper.getMinecraft().options.guiScale
        if (scale == 0) scale = 1000
        var scaleFactor = 0
        while (scaleFactor < scale && Wrapper.getMinecraft().window.width / (scaleFactor + 1) >= 320 && Wrapper.getMinecraft().window.height / (scaleFactor + 1) >= 240) scaleFactor++
        if (scaleFactor == 0) scaleFactor = 1
        return scaleFactor
    }

}
