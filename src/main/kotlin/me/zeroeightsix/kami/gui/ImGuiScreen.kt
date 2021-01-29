package me.zeroeightsix.kami.gui

import imgui.ImGui
import imgui.flag.ImGuiConfigFlags
import me.zeroeightsix.kami.gui.ImguiDSL.without
import me.zeroeightsix.kami.mc
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import org.lwjgl.glfw.GLFW

/**
 * class for a screen for which input should be handled.
 * includes callback handlers
 */
abstract class ImGuiScreen(title: Text) : Screen(title) {

    override fun onClose() {
        ImGui.getIO().configFlags = ImGui.getIO().configFlags or ImGuiConfigFlags.NoMouse
        super.onClose()
    }

    override fun init() {
        super.init()
        ImGui.getIO().configFlags = ImGui.getIO().configFlags without ImGuiConfigFlags.NoMouse
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        val returned = super.keyPressed(keyCode, scanCode, modifiers)
        if (!returned) {
            KamiImgui.imguiGlfw.keyCallback(mc.window.handle, keyCode, scanCode, GLFW.GLFW_PRESS, modifiers)
        }
        return returned
    }

    override fun keyReleased(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        val returned = super.keyReleased(keyCode, scanCode, modifiers)
        if (!returned) {
            KamiImgui.imguiGlfw.keyCallback(mc.window.handle, keyCode, scanCode, GLFW.GLFW_RELEASE, modifiers)
        }
        return returned
    }

    override fun charTyped(chr: Char, keyCode: Int): Boolean {
        val returned = super.charTyped(chr, keyCode)
        if (!returned) {
            KamiImgui.imguiGlfw.charCallback(mc.window.handle, chr.toInt())
            KamiImgui.charQueue.add(chr to keyCode)
        }
        return returned
    }
}