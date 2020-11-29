package me.zeroeightsix.kami.gui

import imgui.ConfigFlag
import imgui.ImGui
import imgui.impl.glfw.ImplGlfw
import imgui.wo
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import org.lwjgl.glfw.GLFW

/**
 * class for a screen for which input should be handled.
 * includes callback handlers
 */
abstract class InteractableScreen(title: Text) : Screen(title) {

    override fun onClose() {
        ImGui.io.configFlags = ImGui.io.configFlags or ConfigFlag.NoMouse.i
        super.onClose()
    }

    override fun init() {
        super.init()
        ImGui.io.configFlags = ImGui.io.configFlags wo ConfigFlag.NoMouse
    }

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
}