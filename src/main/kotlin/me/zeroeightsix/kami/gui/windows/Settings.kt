package me.zeroeightsix.kami.gui.windows

import imgui.ImGui
import imgui.ImGui.button
import imgui.ImGui.colorConvertHSVtoRGB
import imgui.ImGui.colorConvertRGBtoHSV
import imgui.ImGui.colorEdit3
import imgui.ImGui.dragFloat
import imgui.ImGui.dummy
import imgui.ImGui.popID
import imgui.ImGui.pushID
import imgui.ImGui.sameLine
import imgui.ImGui.selectable
import imgui.ImGui.textWrapped
import imgui.flag.ImGuiColorEditFlags
import imgui.flag.ImGuiTabBarFlags
import imgui.flag.ImGuiWindowFlags
import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting
import kotlin.reflect.KMutableProperty0
import me.zeroeightsix.kami.feature.hidden.PrepHandler
import me.zeroeightsix.kami.gui.ImguiDSL.checkbox
import me.zeroeightsix.kami.gui.ImguiDSL.combo
import me.zeroeightsix.kami.gui.ImguiDSL.helpMarker
import me.zeroeightsix.kami.gui.ImguiDSL.tabBar
import me.zeroeightsix.kami.gui.ImguiDSL.tabItem
import me.zeroeightsix.kami.gui.ImguiDSL.window
import me.zeroeightsix.kami.gui.ImguiDSL.wrapSingleFloatArray
import me.zeroeightsix.kami.gui.KamiImgui
import me.zeroeightsix.kami.gui.Themes
import me.zeroeightsix.kami.gui.charButton
import me.zeroeightsix.kami.gui.widgets.EnabledWidgets
import me.zeroeightsix.kami.gui.widgets.TextPinnableWidget
import me.zeroeightsix.kami.gui.windows.modules.ModuleWindowsEditor
import me.zeroeightsix.kami.gui.windows.modules.Modules
import me.zeroeightsix.kami.setting.KamiConfig
import me.zeroeightsix.kami.setting.guiService
import me.zeroeightsix.kami.setting.settingInterface

object Settings {

    @Setting
    var settingsWindowOpen = false

    @Setting
    var commandPrefix = '.'

    @Setting
    var openChatWhenCommandPrefixPressed = true

    // Behaviour
    @Setting
    var modifiersEnabled = false

    @Setting
    var openSettingsInPopup = true

    @Setting // only if openSettingsInPopup = false
    var swapModuleListButtons = false

    @Setting
    var hideModuleDescriptions = false

    @Setting
    var openGuiAnywhere = false

    // Appearance
    @Setting
    var font: Int = 0

    @Setting
    var rainbowMode = false

    @Setting
    var styleIdx = 0

    @Setting
    var borderOffset = 10f

    @Setting
    var rainbowSpeed = 0.1

    @Setting
    var rainbowSaturation = 0.5f

    @Setting
    var rainbowBrightness = 1f

    // Other
    @Setting
    var demoWindowInView = false

    @Setting
    var hudWithDebug = false

    @Setting
    var moduleAlignment = TextPinnableWidget.Alignment.CENTER

    private val themes = Themes.Variants.values().map { it.name.toLowerCase().capitalize() }.toTypedArray()

    init {
        KamiConfig.register(guiService("gui_settings"), this)
    }

    operator fun invoke() {
        fun boolSetting(
            label: String,
            checked: KMutableProperty0<Boolean>,
            description: String,
            block: () -> Unit = {}
        ) {
            checkbox(label, checked, block)
            sameLine()
            helpMarker(description)
        }

        if (settingsWindowOpen) {
            window("Settings", ::settingsWindowOpen, flags = ImGuiWindowFlags.AlwaysAutoResize) {
                tabBar("kami-settings-tabbar", ImGuiTabBarFlags.None) {
                    tabItem("Behaviour") {
                        charButton("Command prefix", ::commandPrefix)
                        sameLine()
                        helpMarker("The character used to denote KAMI commands.")

                        boolSetting(
                            "Open the KAMI GUI anywhere",
                            ::openGuiAnywhere,
                            "Allow the GUI to be opened on any screen, using the same keybind as ingame."
                        )

                        boolSetting(
                            "Open chat when command prefix pressed",
                            ::openChatWhenCommandPrefixPressed,
                            "Opens the chat with the command prefix already inserted when the command prefix is pressed ingame."
                        )

                        boolSetting(
                            "Keybind modifiers",
                            ::modifiersEnabled,
                            "Allows the use of keybinds with modifiers: e.g. chaining CTRL, ALT and K."
                        )
                        boolSetting(
                            "Hide descriptions",
                            ::hideModuleDescriptions,
                            "Hide module descriptions when its settings are opened."
                        )
                        dummy(0f, 5f)
                        if (button("Reset module windows")) {
                            Modules.reset()
                        }
                        if (!ModuleWindowsEditor.open) {
                            sameLine()
                            if (button("Open module windows editor")) {
                                ModuleWindowsEditor.open = true
                            }
                        }
                    }
                    tabItem("Appearance") {
                        showFontSelector()

                        showThemeSelector()

                        KamiConfig.alignmentType.settingInterface?.displayImGui(
                            "Module alignment",
                            this.moduleAlignment
                        )?.let {
                            this.moduleAlignment = it
                        }

                        boolSetting(
                            "Rainbow mode",
                            ::rainbowMode,
                            "If enabled, turns the GUI into a rainbow-coloured mess"
                        ) {
                            Themes.Variants.values()[styleIdx].applyStyle(false)
                        }

                        showBorderOffsetSlider()

                        val speed = floatArrayOf(rainbowSpeed.toFloat())
                        if (dragFloat("Rainbow speed", speed, 0.005f, 0.05f, 1f)) {
                            rainbowSpeed = speed[0].toDouble().coerceIn(0.0, 1.0)
                        }

                        val col = floatArrayOf(
                            PrepHandler.getRainbowHue().toFloat(),
                            rainbowSaturation,
                            rainbowBrightness,
                            1.0f
                        )
                        colorConvertHSVtoRGB(col, col)
                        colorEdit3(
                            "Rainbow colour",
                            col,
                            ImGuiColorEditFlags.DisplayHSV or ImGuiColorEditFlags.NoPicker
                        )
                        colorConvertRGBtoHSV(col, col)
                        rainbowSaturation = col[1]
                        rainbowBrightness = col[2]

                        dummy(0f, 5f)
                        textWrapped("Enabled HUD elements:")
                        EnabledWidgets.enabledButtons()
                    }
                    tabItem("Other") {
                        boolSetting(
                            "Show demo window in 'View'",
                            ::demoWindowInView,
                            "Allows the demo window to be shown through the 'View' submenu of the main menu bar"
                        )
                        boolSetting(
                            "Show HUD with debug screen",
                            ::hudWithDebug,
                            "Shows the HUD even when the debug screen is open"
                        )
                    }
                }
            }
        }
    }

    fun showBorderOffsetSlider(label: String = "Border offset") {
        wrapSingleFloatArray(::borderOffset) {
            dragFloat(
                label,
                it,
                0.1f,
                0f,
                50f,
                "%.0f"
            )
        }
    }

    fun showThemeSelector(label: String = "Theme") {
        combo(label, ::styleIdx, themes) {
            Themes.Variants.values()[it.get()].applyStyle(true)
        }
    }

    fun showFontSelector(label: String = "Font###kami-settings-font-selector") {
        val fontCurrent = KamiImgui.fontNames[font]
        if (ImGui.beginCombo(label, fontCurrent)) {
            KamiImgui.fontNames.forEachIndexed { idx, fontName ->
                pushID(fontName)
                if (selectable(fontName, fontCurrent === fontName)) {
                    ImGui.getIO().setFontDefault(KamiImgui.fonts[fontName])
                    this.font = idx
                }
                popID()
            }
            ImGui.endCombo()
        }
    }
}