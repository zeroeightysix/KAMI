package me.zeroeightsix.kami.gui

import com.google.common.io.ByteStreams
import imgui.ImFont
import imgui.ImFontConfig
import imgui.ImGui
import imgui.gl3.ImGuiImplGl3
import imgui.glfw.ImGuiImplGlfw
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.gui.windows.Settings
import me.zeroeightsix.kami.mc
import me.zeroeightsix.kami.tryOrNull
import net.minecraft.client.util.math.MatrixStack
import org.lwjgl.opengl.GL
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

object KamiImgui {

    val imguiGlfw: ImGuiImplGlfw = ImGuiImplGlfw()
    private val imguiGl: ImGuiImplGl3 = ImGuiImplGl3()
    private val postDrawStack: Stack<(MatrixStack) -> Unit> = Stack()

    val fonts = mutableListOf<ImFont>()

    private const val minecraftiaLocation = "/assets/kami/Minecraftia.ttf"

    init {
        fun addKamiFontFromTTF(filename: String, sizePixels: Float, fontCfg: ImFontConfig): ImFont? {
            val bytes = ByteStreams.toByteArray(javaClass.getResourceAsStream(filename) ?: return null)
            return ImGui.getIO().fonts.addFontFromMemoryTTF(bytes, sizePixels, fontCfg)
        }

        val caps = GL.getCapabilities()
        ImGui.createContext()
        // TODO: check if this works on macOS properly.
        val glslVersion = when {
            caps.OpenGL32 -> {
                150
            }
            caps.OpenGL30 -> { // apparently we might have to skip this one?
                130
            }
            else -> 110
        }
        imguiGlfw.init(mc.window.handle, false)
        imguiGl.init("#version $glslVersion")

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
        )?.let {
            fonts.add(it)
        }
        addKamiFontFromTTF(
            minecraftiaLocation,
            24f,
            fontCfg {
//                oversample put 1
//                pixelSnapH = true
//                glyphOffset = Vec2(0, -2)
            }
        )?.let {
            fonts.add(it)
        }
        ImGui.getIO().fonts.addFontDefault()?.let {
            fonts.add(it)
        }
        ImGui.getIO().fonts.build() // rebuild the font atlas
        imguiGl.updateFontsTexture()

        Themes.Variants.values()[Settings.styleIdx].applyStyle(true)
        val defaultFont = fonts.getOrElse(Settings.font) { fonts.first() }
        ImGui.getIO().setFontDefault(defaultFont)
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
