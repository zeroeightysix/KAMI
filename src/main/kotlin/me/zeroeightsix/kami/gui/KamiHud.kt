package me.zeroeightsix.kami.gui

import glm_.vec2.Vec2d
import imgui.ImGui
import imgui.classes.Context
import imgui.classes.IO
import imgui.impl.gl.ImplGL3
import imgui.impl.glfw.ImplGlfw
import me.zeroeightsix.kami.gui.widgets.EnabledWidgets
import net.minecraft.client.MinecraftClient
import uno.glfw.GlfwWindow

object KamiHud {

    internal var implGl3: ImplGL3
    internal val context: Context
    private val implGlfw: ImplGlfw
    private val io: IO

    init {
        val window = GlfwWindow.from(MinecraftClient.getInstance().window.handle)
        window.makeContextCurrent()
        context = Context()
        implGlfw = ImplGlfw(window, false, null)
        implGl3 = ImplGL3()
        io = ImGui.io
    }

    fun renderHud() {
        frame {
            if (!EnabledWidgets.hideAll) {
                for ((widget, open) in EnabledWidgets.widgets) {
                    if (open.get() && widget.pinned) {
                        widget.showWindow(open)
                    }
                }
            }
        }
    }

    internal fun frame(block: () -> Unit) {
        implGl3.newFrame()
        implGlfw.newFrame()
        ImGui.newFrame()

        try { block() } finally {
            ImGui.render()
            implGl3.renderDrawData(ImGui.drawData!!)
        }
    }

    fun mouseScroll(d: Double, e: Double) {
        ImplGlfw.scrollCallback(Vec2d(d, e))
    }

}