package me.zeroeightsix.kami.gui.windows

import imgui.ImGui
import imgui.ImGui.dragFloat
import imgui.ImGui.dragInt
import imgui.ImGui.sameLine
import imgui.api.demoDebugInformations
import imgui.dsl.button
import imgui.dsl.checkbox
import imgui.dsl.collapsingHeader
import imgui.dsl.window
import me.zeroeightsix.kami.gui.Themes
import me.zeroeightsix.kami.gui.windows.modules.Modules

object KamiSettings {
    
    var settingsWindowOpen = false
    var swapModuleListButtons = false
    var hideModuleDescriptions = false
    var hideModuleMarker = false
    var styleIdx = 0
    var borderOffset = 10f
    var rainbowSpeed = 32
    var rainbowSaturation = 1f
    var rainbowBrightness = 1f

    private val themes = Themes.Variants.values().map { it.name.toLowerCase().capitalize() }

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

                    button("Reset module windows") {
                        Modules.reset()
                    }
                }

                collapsingHeader("GUI") {
                    if (ImGui.combo("Theme", ::styleIdx, themes)) {
                        Themes.Variants.values()[styleIdx].applyStyle()
                    }

                    if (dragInt("Rainbow speed", ::rainbowSpeed, vSpeed = 0.1F, vMin = 1, vMax = 128)) {
                        rainbowSpeed = rainbowSpeed.coerceAtLeast(1) // Do not let users custom edit this below 1
                    }
                    if (dragFloat("Rainbow saturation", ::rainbowSaturation, vSpeed = 0.01F, vMin = 0f, vMax = 1f)) {
                        rainbowSaturation = rainbowSaturation.coerceIn(0f, 1f)
                    }
                    if (dragFloat("Rainbow brightness", ::rainbowBrightness, vSpeed = 0.01F, vMin = 0f, vMax = 1f)) {
                        rainbowBrightness = rainbowBrightness.coerceIn(0f, 1f)
                    }
                }

                collapsingHeader("Overlay") {
                    dragFloat("Border offset", ::borderOffset, vMin = 0f, vMax = 50f, format = "%.0f")
                }
            }
        }
    }

}