package me.zeroeightsix.kami.gui

import com.google.common.io.ByteStreams
import imgui.ImFont
import imgui.ImFontConfig
import imgui.ImGui
import imgui.gl3.ImGuiImplGl3
import imgui.glfw.ImGuiImplGlfw
import java.nio.file.Files
import java.nio.file.Paths
import java.util.Stack
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.gui.windows.Settings
import me.zeroeightsix.kami.mc
import me.zeroeightsix.kami.tryOrNull
import net.minecraft.client.util.math.MatrixStack
import org.lwjgl.opengl.GL

object KamiImgui {

    // Because the imgui bindings don't expose the internal char queue, we store the chars passed to imgui in here.
    // Anything pasted in by the clipboard will not appear in this queue.
    // It is cleared after each `frame` call.
    val charQueue = mutableListOf<Pair<Char, Int/*keycode*/>>()
    val imguiGlfw: ImGuiImplGlfw = ImGuiImplGlfw()
    private val imguiGl: ImGuiImplGl3 = ImGuiImplGl3()
    private val postDrawStack: Stack<(MatrixStack) -> Unit> = Stack()
    private const val INI_FILENAME = "kami-imgui.ini"

    val fonts = mutableMapOf<String, ImFont>()
    val fontNames = arrayOf("Minecraftia 12px", "Minecraftia 24px", "Default")

    private const val minecraftiaLocation = "/assets/kami/Minecraftia.ttf"

    init {
        fun addKamiFontFromTTF(filename: String, sizePixels: Float, fontCfg: ImFontConfig): ImFont? {
            val bytes = ByteStreams.toByteArray(javaClass.getResourceAsStream(filename) ?: return null)
            return ImGui.getIO().fonts.addFontFromMemoryTTF(bytes, sizePixels, fontCfg)
        }

        ImGui.createContext()

        ImGui.getIO().iniFilename = INI_FILENAME
        tryOrNull {
            Files.createFile(Paths.get(INI_FILENAME))
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
                oversampleH = 1
                oversampleV = 1
                pixelSnapH = true
            }
        )?.let {
            fonts.put("Minecraftia 12px", it)
        }
        addKamiFontFromTTF(
            minecraftiaLocation,
            24f,
            fontCfg {
                oversampleH = 1
                oversampleV = 1
                pixelSnapH = true
            }
        )?.let {
            fonts.put("Minecraftia 24px", it)
        }
        ImGui.getIO().fonts.addFontDefault()?.let {
            fonts.put("Default", it)
        }
        ImGui.getIO().fonts.build() // rebuild the font atlas
        imguiGl.updateFontsTexture()

        Themes.Variants.values()[Settings.styleIdx].applyStyle(true)
        val defaultFontName = fontNames.getOrElse(Settings.font) { fontNames.first() }
        ImGui.getIO().setFontDefault(fonts[defaultFontName])

        val caps = GL.getCapabilities()
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
            charQueue.clear()
        }
    }

    fun postDraw(block: (MatrixStack) -> Unit) {
        postDrawStack.push(block)
    }

    fun mouseScroll(d: Double, e: Double) {
        imguiGlfw.scrollCallback(mc.window.handle, d, e)
    }
}