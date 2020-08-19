package me.zeroeightsix.kami.gui.wizard

import glm_.vec2.Vec2
import glm_.vec4.Vec4
import imgui.*
import imgui.ImGui.checkbox
import imgui.ImGui.dummy
import imgui.ImGui.io
import imgui.ImGui.openPopup
import imgui.ImGui.popItemFlag
import imgui.ImGui.popStyleColor
import imgui.ImGui.popStyleVar
import imgui.ImGui.pushItemFlag
import imgui.ImGui.pushStyleColor
import imgui.ImGui.pushStyleVar
import imgui.ImGui.sameLine
import imgui.ImGui.separator
import imgui.ImGui.setNextWindowPos
import imgui.ImGui.text
import imgui.ImGui.textWrapped
import imgui.dsl.button
import imgui.dsl.popupModal
import imgui.dsl.radioButton
import imgui.internal.ItemFlag
import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting
import me.zeroeightsix.kami.conditionalWrap
import me.zeroeightsix.kami.feature.FindSettings
import me.zeroeightsix.kami.feature.module.Aura
import me.zeroeightsix.kami.gui.KamiGuiScreen
import me.zeroeightsix.kami.gui.Themes
import me.zeroeightsix.kami.gui.widgets.EnabledWidgets
import me.zeroeightsix.kami.gui.windows.Settings
import me.zeroeightsix.kami.gui.windows.modules.Modules
import me.zeroeightsix.kami.to

@FindSettings
object Wizard {

    @Setting
    var firstTime = true

    val pages = listOf({
        text("Welcome to KAMI!")
        text("This wizard is going to take you through setting up the GUI to your liking.")
        dummy(Vec2(10))
        text("Everything set by the wizard can be manually changed later through the Settings menu.")
    }, {
        text("Please select your preferred theme and font.")
        if (ImGui.combo("Theme", Settings::styleIdx, Settings.themes)) {
            Themes.Variants.values()[Settings.styleIdx].applyStyle()
        }
        Settings.showFontSelector("Font###kami-settings-font-selector")

        pushStyleColor(Col.Text, Vec4(.7f, .7f, .7f, 1f))
        text("%s", "GUI is visible in the background")
        popStyleColor()

        KamiGuiScreen() // Show the full GUI
    }, {
        text("How do you want your module windows to be set up?")

        radioButton("Per category", Modules.preferCategoryWindows) {
            Modules.preferCategoryWindows = true
            Modules.windows = Modules.getDefaultWindows()
        }
        radioButton("Everything in one window", !Modules.preferCategoryWindows) {
            Modules.preferCategoryWindows = false
            Modules.windows = Modules.getDefaultWindows()
        }

        pushStyleColor(Col.Text, Vec4(.7f, .7f, .7f, 1f))
        textWrapped(
            "%s",
            "The module windows in KAMI are fully customizable. If neither choice appeals to you, you can manually reorganise the module windows through the module window editor."
        )
        textWrapped("%s", "The module window editor may be accessed through the `View` menu in the top menu bar.")
        popStyleColor()
    }, {
        text("Would you rather have settings appear in a popup, or embedded in the modules window?")

        separator()

        checkbox("In a popup", Settings::openSettingsInPopup)
        if (checkbox("Embedded in the modules window", BooleanArray(1) { !Settings.openSettingsInPopup })) {
            Settings.openSettingsInPopup = false
        }

        if (!Settings.openSettingsInPopup) {
            separator()

            text("Would you rather left-click or right-click a module to toggle it?")
            text("The other button will toggle its settings.")

            separator()

            checkbox("Left-click to toggle modules", Settings::swapModuleListButtons)
            if (checkbox("Right-click to toggle modules", BooleanArray(1) { !Settings.swapModuleListButtons })) {
                Settings.swapModuleListButtons = false
            }
        }

        separator()

        pushStyleColor(Col.Text, Vec4(.7f, .7f, .7f, 1f))
        val leftToggle = Settings.openSettingsInPopup || Settings.swapModuleListButtons
        text(
            "%s-click to toggle, %s-click to open settings.",
            if (leftToggle) {
                "Left"
            } else {
                "Right"
            },
            if (!leftToggle) {
                "left"
            } else {
                "right"
            }
        )
        text("%s", "Try it out:")
        popStyleColor()

        Modules.collapsibleModule(Aura, Modules.ModuleWindow("", Aura), "")

        separator()
    }, {
        text("Should KAMI enable usage of modifier keys in binds?")
        text("Enabling this will make pressing e.g. 'Q' different from 'CTRL+Q'.")
        textWrapped("This has the sometimes unintended side effect of e.g. being unable to toggle a module while sneaking, if sneaking is bound to a modifier key.")
        checkbox("Enable modifier keys", Settings::modifiersEnabled)

        separator()

        pushStyleColor(Col.Text, Vec4(.7f, .7f, .7f, 1f))
        text("%s", "Assuming 'K' is bound to Aura,")
        text("%s", "And 'CTRL+Q' is bound to Brightness,")
        dummy(Vec2(10))
        val not = Settings.modifiersEnabled.to(" NOT ", " ")
        text("%s", "Pressing K WILL toggle Aura.")
        text("%s", "Pressing SHIFT+K WILL${not}toggle Aura.")
        dummy(Vec2(10))
        text("%s", "Pressing CTRL+Q WILL toggle Brightness.")
        text("%s", "Pressing Q WILL${not}toggle Brightness.")
        popStyleColor()

        separator()
    }, {
        text("How far from the edge should HUD elements be rendered?")
        ImGui.dragFloat("Border offset", Settings::borderOffset, vSpeed = 0.1f, vMin = 0f, vMax = 50f, format = "%.0f")
        separator()
        text("Which elements should be shown in the HUD?")
        EnabledWidgets.enabledButtons()
        separator()
        KamiGuiScreen.showWidgets(false)
    }, {
        firstTime = false
    })

    var currentPage = 0

    /**
     * Returns `true` if the wizard was opened
     */
    operator fun invoke(): Boolean {
        if (firstTime) {
            openPopup("Setup wizard")
            setNextWindowPos(Vec2(io.displaySize.x * 0.5f, io.displaySize.y * 0.5f), Cond.Always, Vec2(0.5f))
            popupModal("Setup wizard", extraFlags = WindowFlag.AlwaysAutoResize or WindowFlag.NoTitleBar or WindowFlag.NoMove.i) {
                pages[currentPage]()
                (currentPage == 0).conditionalWrap(
                    {
                        pushItemFlag(ItemFlag.Disabled.i, true)
                        pushStyleVar(StyleVar.Alpha, ImGui.style.alpha * 0.5f)
                    },
                    {
                        button("Previous", Vec2(100, 0)) {
                            currentPage--
                        }
                    },
                    {
                        popItemFlag()
                        popStyleVar()
                    }
                )
                sameLine()
                button("Next", Vec2(100, 0)) {
                    currentPage++
                }
            }
        }

        return firstTime
    }

}
