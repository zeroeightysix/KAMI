package me.zeroeightsix.kami.gui

import imgui.dsl.mainMenuBar
import imgui.dsl.menu
import imgui.dsl.menuItem
import me.zeroeightsix.kami.gui.widgets.EnabledWidgets
import me.zeroeightsix.kami.gui.windows.KamiSettings

object MenuBar {

    operator fun invoke() = mainMenuBar {
        EnabledWidgets()
        menu("GUI") {
            menuItem("Settings", "", selected = KamiSettings.settingsWindowOpen) {
                KamiSettings.settingsWindowOpen = !KamiSettings.settingsWindowOpen
            }
        }
    }

}