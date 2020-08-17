package me.zeroeightsix.kami.gui

import imgui.ConfigFlag
import imgui.ImGui
import imgui.impl.glfw.ImplGlfw
import imgui.wo
import me.zeroeightsix.kami.gui.widgets.EnabledWidgets
import me.zeroeightsix.kami.gui.windows.Settings
import me.zeroeightsix.kami.gui.windows.modules.Modules
import me.zeroeightsix.kami.gui.wizard.Wizard
import me.zeroeightsix.kami.util.Texts.lit
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.Text
import org.lwjgl.glfw.GLFW

object KamiGuiScreen : Screen(lit("Kami GUI") as Text?) {

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
        for ((widget, open) in EnabledWidgets.widgets) {
            if (open.get()) {
                widget.showWindow(open, limitY)
            }
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
