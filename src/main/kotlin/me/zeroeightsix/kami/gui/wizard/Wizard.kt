package me.zeroeightsix.kami.gui.wizard

import glm_.vec2.Vec2
import glm_.vec4.Vec4
import imgui.*
import imgui.ImGui.checkbox
import imgui.ImGui.dummy
import imgui.ImGui.io
import imgui.dsl.popupModal
import imgui.ImGui.text
import imgui.ImGui.openPopup
import imgui.ImGui.popItemFlag
import imgui.ImGui.popStyleColor
import imgui.ImGui.popStyleVar
import imgui.ImGui.pushItemFlag
import imgui.ImGui.pushStyleVar
import imgui.ImGui.sameLine
import imgui.ImGui.setNextWindowPos
import imgui.ImGui.pushStyleColor
import imgui.ImGui.selectable
import imgui.ImGui.separator
import imgui.ImGui.textWrapped
import imgui.dsl.button
import imgui.internal.ItemFlag
import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting
import me.zeroeightsix.kami.conditionalWrap
import me.zeroeightsix.kami.feature.module.Aura
import me.zeroeightsix.kami.gui.KamiGuiScreen
import me.zeroeightsix.kami.gui.Themes
import me.zeroeightsix.kami.gui.windows.GraphicalSettings
import me.zeroeightsix.kami.gui.windows.modules.ModuleSettings
import me.zeroeightsix.kami.gui.windows.modules.Modules
import me.zeroeightsix.kami.to

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
        if (ImGui.combo("Theme", GraphicalSettings::styleIdx, GraphicalSettings.themes)) {
            Themes.Variants.values()[GraphicalSettings.styleIdx].applyStyle()
        }
        GraphicalSettings.showFontSelector("Font###kami-settings-font-selector")

        pushStyleColor(Col.Text, Vec4(.7f, .7f, .7f, 1f))
        textWrapped("%s", "GUI is visible in the background")
        popStyleColor()
        
        KamiGuiScreen() // Show the full GUI
    }, {
        text("Would you rather left-click or right-click a module to toggle it?")
        text("The other button will toggle its settings.")

        separator()

        checkbox("Left-click to toggle modules", GraphicalSettings::swapModuleListButtons)
        if (checkbox("Right-click to toggle modules", BooleanArray(1) { !GraphicalSettings.swapModuleListButtons })) {
            GraphicalSettings.swapModuleListButtons = false
        }

        separator()

        Modules.collapsibleModule(Aura, Modules.ModuleWindow("", null, Aura), "")

        separator()
    }, {
        text("Should KAMI enable usage of modifier keys in binds?")
        text("Enabling this will make pressing e.g. 'Q' different from 'CTRL+Q'.")
        textWrapped("This has the sometimes unintended side effect of e.g. being unable to toggle a module while sneaking, if sneaking is bound to a modifier key.")
        checkbox("Enable modifier keys", GraphicalSettings::modifiersEnabled)
        
        separator()

        pushStyleColor(Col.Text, Vec4(.7f, .7f, .7f, 1f))
        text("%s", "Assuming 'K' is bound to Aura,")
        text("%s", "And 'CTRL+Q' is bound to Brightness,")
        dummy(Vec2(10))
        val not = GraphicalSettings.modifiersEnabled.to(" NOT ", " ")
        text("%s", "Pressing K WILL toggle Aura.")
        text("%s", "Pressing SHIFT+K WILL${not}toggle Aura.")
        dummy(Vec2(10))
        text("%s", "Pressing CTRL+Q WILL toggle Brightness.")
        text("%s", "Pressing Q WILL${not}toggle Brightness.")
        popStyleColor()

        separator()
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