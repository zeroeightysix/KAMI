package me.zeroeightsix.kami.gui

import imgui.ImGui
import imgui.impl.gl.ImplGL3
import imgui.impl.glfw.ImplGlfw
import me.zeroeightsix.kami.gui.KamiHud.context
import me.zeroeightsix.kami.gui.KamiHud.implGl3
import me.zeroeightsix.kami.util.Texts.lit
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import org.lwjgl.glfw.GLFW

class KamiGuiScreen : Screen(lit("Kami GUI") as Text?) {

    var demoWindowVisible = false

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        val returned = super.keyPressed(keyCode, scanCode, modifiers)
        if (!returned && context.wantCaptureKeyboardNextFrame != -1) {
            ImplGlfw.keyCallback(keyCode, scanCode, GLFW.GLFW_PRESS, modifiers)
        }
        return returned
    }

    override fun keyReleased(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        val returned = super.keyReleased(keyCode, scanCode, modifiers)
        if (!returned && context.wantCaptureKeyboardNextFrame != -1) {
            ImplGlfw.keyCallback(keyCode, scanCode, GLFW.GLFW_RELEASE, modifiers)
        }
        return returned
    }

    override fun charTyped(chr: Char, keyCode: Int): Boolean {
        val returned = super.charTyped(chr, keyCode)
        if (!returned && context.wantCaptureKeyboardNextFrame != -1) {
            ImplGlfw.charCallback(chr.toInt())
        }
        return returned
    }

    override fun render(mouseX: Int, mouseY: Int, delta: Float) {
        super.render(mouseX, mouseY, delta)

        KamiHud.frame {
            with (ImGui) {
                textWrapped("Hello world!")

                checkbox("Demo Window", ::demoWindowVisible)

                if (demoWindowVisible) {
                    showDemoWindow(::demoWindowVisible)
                }

                if (KamiHud.informationVisible) {
                    Information(KamiHud::informationVisible)
                }
            }
        }
    }

    fun reload() {
        implGl3 = ImplGL3()
    }

}