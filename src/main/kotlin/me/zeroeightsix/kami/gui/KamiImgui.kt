package me.zeroeightsix.kami.gui

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
import java.util.Stack

object KamiImgui {

    private val imguiGl: ImGuiImplGl3
    val imguiGlfw: ImGuiImplGlfw
    private val postDrawStack: Stack<(MatrixStack) -> Unit> = Stack()

    private const val minecraftiaLocation = "/assets/kami/Minecraftia.ttf"

    init {
        fun addKamiFontFromTTF(filename: String, sizePixels: Float, fontCfg: ImFontConfig) {
            val bytes = javaClass.getResourceAsStream(filename)?.readAllBytes()
            ImGui.getIO().fonts.addFontFromMemoryTTF(bytes, sizePixels, fontCfg)
        }

        imguiGl = ImGuiImplGl3()
        imguiGl.init()
        ImGui.createContext()

        imguiGlfw = ImGuiImplGlfw()
        imguiGlfw.init(mc.window.handle, false)
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
