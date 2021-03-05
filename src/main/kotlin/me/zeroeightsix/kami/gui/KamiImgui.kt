package me.zeroeightsix.kami.gui

import com.google.common.io.ByteStreams
import imgui.ImFont
import imgui.ImFontConfig
import imgui.ImGui
import imgui.flag.ImGuiConfigFlags
import imgui.gl3.ImGuiImplGl3
import imgui.glfw.ImGuiImplGlfw
import java.nio.file.Files
import java.nio.file.Paths
import java.util.Stack
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.gui.windows.Settings
import me.zeroeightsix.kami.mc
import me.zeroeightsix.kami.tryOrNull
import me.zeroeightsix.kami.util.Bind
import net.minecraft.client.util.math.MatrixStack
import org.lwjgl.opengl.GL

object KamiImgui {

    // Because the imgui bindings don't expose the internal char queue, we store the chars passed to imgui in here.
    // Anything pasted in by the clipboard will not appear in this queue.
    // It is cleared after each `frame` call.
    val charQueue = mutableListOf<Pair<Char, Int/*keycode*/>>()
    val keyQueue = mutableListOf<Bind.Code>()
    val imguiGlfw: ImGuiImplGlfw = ImGuiImplGlfw()
    private val imguiGl: ImGuiImplGl3 = ImGuiImplGl3()
    private val postDrawStack: Stack<(MatrixStack) -> Unit> = Stack()
    private const val INI_FILENAME = "kami-imgui.ini"

    val fonts = mutableMapOf<String, ImFont>()
    val fontNames = arrayOf("Minecraftia", "Default")

    private const val minecraftiaLocation = "/assets/kami/Minecraftia.ttf"

    fun init() {
        fun loadFontFromResources(filename: String): ByteArray? {
            return ByteStreams.toByteArray(javaClass.getResourceAsStream(filename) ?: return null)
        }
        fun addKamiFontFromTTF(bytes: ByteArray, sizePixels: Float, fontCfg: ImFontConfig): ImFont? {
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

        loadFontFromResources(minecraftiaLocation)?.let { bytes ->
            addKamiFontFromTTF(
                bytes,
                Settings.fontSize,
                fontCfg {
                    oversampleH = 1
                    oversampleV = 1
                    pixelSnapH = true
                }
            )?.let {
                fonts.put("Minecraftia", it)
            }
        }

        ImGui.getIO().fonts.addFontDefault()?.let {
            fonts.put("Default", it)
        }
        ImGui.getIO().fonts.build() // rebuild the font atlas
        imguiGl.updateFontsTexture()

        Themes.Variants.values()[Settings.styleIdx].applyStyle(true)
        val defaultFontName = fontNames.getOrElse(Settings.font) { fontNames.first() }
        ImGui.getIO().setFontDefault(fonts[defaultFontName])

        ImGui.getIO().addConfigFlags(ImGuiConfigFlags.DockingEnable)

        imguiGlfw.init(mc.window.handle, false)
        // Force 110 shaders since this is what base Minecraft uses to avoid bugs in Intel drivers.
        imguiGl.init("#version 110")
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
            keyQueue.clear()
        }
    }

    fun postDraw(block: (MatrixStack) -> Unit) {
        postDrawStack.push(block)
    }

    fun mouseScroll(d: Double, e: Double) {
        imguiGlfw.scrollCallback(mc.window.handle, d, e)
    }
}
