package me.zeroeightsix.kami.gui.windows

import imgui.ImGui.showDemoWindow
import imgui.dsl.checkbox
import imgui.dsl.window

object KamiDebugWindow {
    
    var showDemoWindow = false

    operator fun invoke() = window("Kami debug") {
        checkbox("Show demo window", ::showDemoWindow) {}
        if (showDemoWindow) {
            showDemoWindow(::showDemoWindow)
        }
    }

}