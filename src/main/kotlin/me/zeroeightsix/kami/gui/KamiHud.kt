package me.zeroeightsix.kami.gui

import glm_.c
import glm_.vec2.Vec2
import glm_.vec2.Vec2d
import imgui.ImGui
import imgui.classes.Context
import imgui.classes.IO
import imgui.font.FontConfig
import imgui.impl.gl.GLInterface
import imgui.impl.gl.ImplBestGL
import imgui.impl.glfw.ImplGlfw
import me.zeroeightsix.kami.gui.widgets.EnabledWidgets
import me.zeroeightsix.kami.gui.widgets.PinnableWidget
import me.zeroeightsix.kami.gui.windows.Settings
import me.zeroeightsix.kami.mc
import me.zeroeightsix.kami.tempSet
import me.zeroeightsix.kami.util.Wrapper
import net.minecraft.client.util.math.MatrixStack
import uno.glfw.GlfwWindow
import java.util.Stack

object KamiHud {

    internal var implGl: GLInterface
    internal val context: Context
    private val implGlfw: ImplGlfw
    private val io: IO
    private val postDrawStack: Stack<(MatrixStack) -> Unit>

    private const val minecraftiaLocation = "/assets/kami/Minecraftia.ttf"

    init {
        fun addKamiFontFromTTF(filename: String, sizePixels: Float, fontCfg: FontConfig) {
            val chars = javaClass.getResourceAsStream(minecraftiaLocation)?.let {
                val bytes = it.readBytes()
                CharArray(bytes.size) { bytes[it].c }
            } ?: return
            if (fontCfg.name.isEmpty())
            // Store a short copy of filename into into the font name for convenience
                fontCfg.name = "${filename.substringAfterLast('/')}, %.0fpx".format(ImGui.style.locale, sizePixels)
            ImGui.io.fonts.addFontFromMemoryTTF(chars, sizePixels, fontCfg, arrayOf())
        }

        val window = GlfwWindow.from(mc.window.handle)
        window.makeContextCurrent()
        context = Context()
        implGlfw = ImplGlfw(window, false, null)
        implGl = ImplBestGL()
        io = ImGui.io
        io.iniFilename = "kami-imgui.ini"

        fun fontCfg(block: FontConfig.() -> Unit): FontConfig {
            val fontCfg = FontConfig()
            fontCfg.block()
            return fontCfg
        }

        addKamiFontFromTTF(
            minecraftiaLocation,
            12f,
            fontCfg {
                oversample put 1
                pixelSnapH = true
                glyphOffset = Vec2(0, -2)
            }
        )
        addKamiFontFromTTF(
            minecraftiaLocation,
            24f,
            fontCfg {
                oversample put 1
                pixelSnapH = true
                glyphOffset = Vec2(0, -2)
            }
        )
        ImGui.io.fonts.addFontDefault()

        Themes.Variants.values()[Settings.styleIdx].applyStyle(true)
        ImGui.io.fontDefault = ImGui.io.fonts.fonts.getOrElse(Settings.font) { ImGui.io.fonts.fonts.first() }

        postDrawStack = Stack()
    }

    fun renderHud(matrixStack: MatrixStack) {
        if (mc.options.hudHidden) return
        frame(matrixStack) {
            if (!EnabledWidgets.hideAll) {
                PinnableWidget.Companion::drawFadedBackground.tempSet(false) {
                    EnabledWidgets.widgets.removeAll { widget ->
                        widget.open && widget.pinned && widget.showWindow(false)
                    }
                }
            }
        }
    }

    internal fun frame(matrices: MatrixStack, block: () -> Unit) {
        implGl.newFrame()
        implGlfw.newFrame()
        ImGui.newFrame()

        try {
            block()
        } finally {
            ImGui.render()
            implGl.renderDrawData(ImGui.drawData!!)
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

    fun getScale() = mc.window.scaleFactor.toInt()
}
