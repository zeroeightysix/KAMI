package me.zeroeightsix.kami.gui

import com.google.common.io.ByteStreams
import imgui.ImFontConfig
import imgui.ImGui
import imgui.gl3.ImGuiImplGl3
import imgui.glfw.ImGuiImplGlfw
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.gui.windows.Settings
import me.zeroeightsix.kami.mc
import me.zeroeightsix.kami.tryOrNull
import net.minecraft.client.util.math.MatrixStack
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

object KamiImgui {

    val imguiGlfw: ImGuiImplGlfw = ImGuiImplGlfw()
    private val imguiGl: ImGuiImplGl3 = ImGuiImplGl3()
    private val postDrawStack: Stack<(MatrixStack) -> Unit> = Stack()

    private const val minecraftiaLocation = "/assets/kami/Minecraftia.ttf"

    init {
        fun addKamiFontFromTTF(filename: String, sizePixels: Float, fontCfg: ImFontConfig) {
            val bytes = ByteStreams.toByteArray(javaClass.getResourceAsStream(filename) ?: return)
            ImGui.getIO().fonts.addFontFromMemoryTTF(bytes, sizePixels, fontCfg)
        }

        ImGui.createContext()
        imguiGlfw.init(mc.window.handle, false)
        imguiGl.init()

        val iniFilename = "kami-imgui.ini"
        ImGui.getIO().iniFilename = iniFilename
        tryOrNull {
            Files.createFile(Paths.get(iniFilename))
            "Created imgui .ini file!"
        }?.let { KamiMod.log.info(it) }

        fun fontCfg(block: ImFontConfig.() -> Unit): ImFontConfig {
            val fontCfg = ImFontConfig()
            fontCfg.block()
            return fontCfg
        }

        addKamiFontFromTTF(
            minecraftiaLocation,
            12f,
            fontCfg {
//                oversample put 1
//                pixelSnapH = true
//                glyphOffset = Vec2(0, -2)
            }
        )
        addKamiFontFromTTF(
            minecraftiaLocation,
            24f,
            fontCfg {
//                oversample put 1
//                pixelSnapH = true
//                glyphOffset = Vec2(0, -2)
            }
        )
        ImGui.getIO().fonts.addFontDefault()
        ImGui.getIO().fonts.build() // rebuild the font atlas

        Themes.Variants.values()[Settings.styleIdx].applyStyle(true)
//        ImGui.getIO().setFontDefault(ImGui.getIO().fonts.)
//        ImGui.io.fontDefault = ImGui.io.fonts.fonts.getOrElse(Settings.font) { ImGui.io.fonts.fonts.first() }
    }

    internal fun frame(matrices: MatrixStack, block: () -> Unit) {
        imguiGlfw.newFrame()
        ImGui.newFrame()

        try {
            block()
        } finally {
            ImGui.render()
            imguiGl.renderDrawData(ImGui.getDrawData())
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
        imguiGlfw.scrollCallback(mc.window.handle, d, e)
    }
}
