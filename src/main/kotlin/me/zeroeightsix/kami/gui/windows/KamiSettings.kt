package me.zeroeightsix.kami.gui.windows

import imgui.ImGui.sameLine
import imgui.api.demoDebugInformations
import imgui.dsl.checkbox
import imgui.dsl.window

object KamiSettings {
    
    var settingsWindowOpen = false
    var swapModuleListButtons = false

    operator fun invoke() {
        if (settingsWindowOpen) {
            window("Settings", ::settingsWindowOpen) {
                checkbox("Swap module list buttons", ::swapModuleListButtons) {}
                sameLine()
                demoDebugInformations.helpMarker("When enabled, left clicking modules will reveal their settings menu. Right clicking will toggle the module.")
            }
        }
    }
    
}