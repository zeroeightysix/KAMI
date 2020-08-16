package me.zeroeightsix.kami.gui.windows

import glm_.vec2.Vec2
import imgui.ImGui
import imgui.ImGui.dragFloat
import imgui.ImGui.dragInt
import imgui.ImGui.dummy
import imgui.ImGui.popID
import imgui.ImGui.pushID
import imgui.ImGui.sameLine
import imgui.ImGui.textWrapped
import imgui.TabBarFlag
import imgui.WindowFlag
import imgui.api.demoDebugInformations
import imgui.dsl.button
import imgui.dsl.checkbox
import imgui.dsl.tabBar
import imgui.dsl.tabItem
import imgui.dsl.window
import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting
import me.zeroeightsix.kami.feature.FindSettings
import me.zeroeightsix.kami.gui.Themes
import me.zeroeightsix.kami.gui.widgets.EnabledWidgets
import me.zeroeightsix.kami.gui.windows.modules.ModuleWindowsEditor
import me.zeroeightsix.kami.gui.windows.modules.Modules
import kotlin.reflect.KMutableProperty0

@FindSettings
object Settings {

    @Setting
    var settingsWindowOpen = false

    @Setting
    var commandPrefix = '.'

    // Behaviour
    @Setting
    var modifiersEnabled = false

    @Setting
    var swapModuleListButtons = false

    @Setting
    var hideModuleDescriptions = false

    @Setting
    var openSettingsInPopup = true

    // Appearance
    @Setting
    var font: Int = 0

    @Setting
    var styleIdx = 0

    @Setting
    var borderOffset = 10f

    @Setting
    var rainbowSpeed = 32

    @Setting
    var rainbowSaturation = 1f

    @Setting
    var rainbowBrightness = 1f

    // Other
    @Setting
    var demoWindowVisible = false

    @Setting
    var hudWithDebug = false

    val themes = Themes.Variants.values().map { it.name.toLowerCase().capitalize() }

    operator fun invoke() {
        fun setting(label: String, checked: KMutableProperty0<Boolean>, description: String) {
            checkbox(label, checked) {}
            sameLine()
            demoDebugInformations.helpMarker(description)
        }

        if (settingsWindowOpen) {
            window("Settings", ::settingsWindowOpen, flags = WindowFlag.AlwaysAutoResize.i) {
                tabBar("kami-settings-tabbar", TabBarFlag.None.i) {
                    tabItem("Behaviour") {
                        setting(
                            "Keybind modifiers",
                            ::modifiersEnabled,
                            "Allows the use of keybinds with modifiers: e.g. chaining CTRL, ALT and K."
                        )
                        setting(
                            "Settings popup",
                            ::openSettingsInPopup,
                            "Show module settings in a popup instead of a collapsible"
                        )
                        setting(
                            "Swap list buttons",
                            ::swapModuleListButtons,
                            "When enabled, right clicking modules will reveal their settings menu. Left clicking will toggle the module."
                        )
                        setting(
                            "Hide descriptions",
                            ::hideModuleDescriptions,
                            "Hide module descriptions when its settings are opened."
                        )
                        dummy(Vec2(0, 5))
                        button("Reset module windows") {
                            Modules.reset()
                        }
                        if (!ModuleWindowsEditor.open) {
                            sameLine()
                            button("Open module windows editor") {
                                ModuleWindowsEditor.open = true
                            }
                        }
                    }
                    tabItem("Appearance") {
                        showFontSelector("Font###kami-settings-font-selector")

                        if (ImGui.combo("Theme", ::styleIdx, themes)) {
                            Themes.Variants.values()[styleIdx].applyStyle()
                        }

                        dragFloat("Border offset", ::borderOffset, vMin = 0f, vMax = 50f, format = "%.0f")

                        if (dragInt("Rainbow speed", ::rainbowSpeed, vSpeed = 0.1F, vMin = 1, vMax = 128)) {
                            rainbowSpeed = rainbowSpeed.coerceAtLeast(1) // Do not let users custom edit this below 1
                        }
                        if (dragFloat(
                                "Rainbow saturation",
                                ::rainbowSaturation,
                                vSpeed = 0.01F,
                                vMin = 0f,
                                vMax = 1f
                            )
                        ) {
                            rainbowSaturation = rainbowSaturation.coerceIn(0f, 1f)
                        }
                        if (dragFloat(
                                "Rainbow brightness",
                                ::rainbowBrightness,
                                vSpeed = 0.01F,
                                vMin = 0f,
                                vMax = 1f
                            )
                        ) {
                            rainbowBrightness = rainbowBrightness.coerceIn(0f, 1f)
                        }

                        dummy(Vec2(0, 5))
                        textWrapped("Enabled HUD elements:")
                        EnabledWidgets.enabledButtons()
                    }
                    tabItem("Other") {
                        setting(
                            "Show demo window in 'View'",
                            ::demoWindowVisible,
                            "Allows the demo window to be shown through the 'View' submenu of the main menu bar"
                        )
                        setting(
                            "Show HUD with debug screen",
                            ::hudWithDebug,
                            "Shows the HUD even when the debug screen is open"
                        )
                    }
                }
            }
        }
    }

    fun showFontSelector(label: String) {
        val fontCurrent = ImGui.font
        if (ImGui.beginCombo(label, fontCurrent.debugName)) {
            ImGui.io.fonts.fonts.forEachIndexed { idx, font ->
                pushID(font)
                if (ImGui.selectable(font.debugName, font === fontCurrent)) {
                    ImGui.io.fontDefault = font
                    this.font = idx
                }
                popID()
            }
            ImGui.endCombo()
        }
    }

}
