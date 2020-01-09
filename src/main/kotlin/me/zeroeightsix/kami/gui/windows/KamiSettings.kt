package me.zeroeightsix.kami.gui.windows

import imgui.ImGui.sameLine
import imgui.api.demoDebugInformations
import imgui.dsl.checkbox
import imgui.dsl.collapsingHeader
import imgui.dsl.window

object KamiSettings {
    
    var settingsWindowOpen = false
    var swapModuleListButtons = false
    var hideModuleDescriptions = false
    var hideModuleMarker = false

    operator fun invoke() {
        if (settingsWindowOpen) {
            window("Settings", ::settingsWindowOpen) {
                collapsingHeader("Module windows") {
                    checkbox("Swap list buttons", ::swapModuleListButtons) {}
                    sameLine()
                    demoDebugInformations.helpMarker("When enabled, right clicking modules will reveal their settings menu. Left clicking will toggle the module.")

                    checkbox("Hide descriptions", ::hideModuleDescriptions) {}
                    sameLine()
                    demoDebugInformations.helpMarker("Hide module descriptions when its settings are opened.")

                    checkbox("Hide help marker", ::hideModuleMarker) {}
                    sameLine()
                    demoDebugInformations.helpMarker("Hide the help marker (such as the one you are hovering right now) in module settings.")
                }

            }
        }
    }
    
}