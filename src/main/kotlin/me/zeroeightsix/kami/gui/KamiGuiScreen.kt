package me.zeroeightsix.kami.gui

import glm_.vec4.Vec4
import imgui.Col
import imgui.ConfigFlag
import imgui.ImGui
import imgui.impl.glfw.ImplGlfw
import imgui.wo
import me.zeroeightsix.kami.feature.hidden.PrepHandler
import me.zeroeightsix.kami.gui.widgets.EnabledWidgets
import me.zeroeightsix.kami.gui.windows.Settings
import me.zeroeightsix.kami.gui.windows.modules.Modules
import me.zeroeightsix.kami.gui.wizard.Wizard
import me.zeroeightsix.kami.util.text
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.util.math.MatrixStack
import org.lwjgl.glfw.GLFW

object KamiGuiScreen : Screen(text(null, "Kami GUI")) {

    val colourIndices = Col.values().map { it.i }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        val returned = super.keyPressed(keyCode, scanCode, modifiers)
        if (!returned) {
            ImplGlfw.keyCallback(keyCode, scanCode, GLFW.GLFW_PRESS, modifiers)
        }
        return returned
    }

    override fun keyReleased(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        val returned = super.keyReleased(keyCode, scanCode, modifiers)
        if (!returned) {
            ImplGlfw.keyCallback(keyCode, scanCode, GLFW.GLFW_RELEASE, modifiers)
        }
        return returned
    }

    override fun charTyped(chr: Char, keyCode: Int): Boolean {
        val returned = super.charTyped(chr, keyCode)
        if (!returned) {
            ImplGlfw.charCallback(chr.toInt())
        }
        return returned
    }

    override fun render(matrices: MatrixStack?, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(matrices, mouseX, mouseY, delta)

        KamiHud.frame(matrices!!) {
            if (Wizard()) return@frame;

            this()
        }
    }

    operator fun invoke() {
        if (Settings.rainbowMode) {
            Themes.Variants.values()[Settings.styleIdx].applyStyle()
            val colors = ImGui.style.colors
            colourIndices.forEach { idx ->
                val col = colors[idx]
                val buf = FloatArray(3)
                ImGui.colorConvertRGBtoHSV(col.toVec3().toFloatArray(), buf)
                buf[0] = PrepHandler.getRainbowHue(buf[0].toDouble()).toFloat()
                ImGui.colorConvertHSVtoRGB(buf, buf)
                colors[idx] = Vec4(buf[0], buf[1], buf[2], col[3])
            }
        }

        // Draw the main menu bar.
        MenuBar()
        // Debug window (theme, demo window)
        if (View.demoWindowVisible) {
            ImGui.showDemoWindow(View::demoWindowVisible)
        }
        // Draw all module windows
        Modules()
        // Draw the settings
        Settings()

        if (!EnabledWidgets.hideAll) {
            showWidgets()
        }
    }

    fun showWidgets(limitY: Boolean = true) {
        EnabledWidgets.widgets.removeAll {
            it.open && it.showWindow(limitY)
        }
    }

    override fun onClose() {
        ImGui.io.configFlags = ImGui.io.configFlags or ConfigFlag.NoMouse.i
        super.onClose()
    }

    override fun init() {
        super.init()
        ImGui.io.configFlags = ImGui.io.configFlags wo ConfigFlag.NoMouse.i
    }

}
