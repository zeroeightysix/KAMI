package me.zeroeightsix.kami.gui

import imgui.dsl.mainMenuBar
import imgui.dsl.menu
import imgui.dsl.menuItem
import me.zeroeightsix.kami.gui.View.demoWindowVisible
import me.zeroeightsix.kami.gui.View.modulesOpen
import me.zeroeightsix.kami.gui.widgets.EnabledWidgets
import me.zeroeightsix.kami.gui.windows.KamiSettings

object View {
    var modulesOpen = true
    var demoWindowVisible = false
}

object MenuBar {

    operator fun invoke() = mainMenuBar {
        EnabledWidgets()
        menu("View") {
            menuItem("Settings", "", selected = KamiSettings.settingsWindowOpen) {
                KamiSettings.settingsWindowOpen = !KamiSettings.settingsWindowOpen
            }
            menuItem("Modules", "", selected = modulesOpen) {
                modulesOpen = !modulesOpen
            }
            menuItem("Demo window", "", selected = demoWindowVisible) {
                demoWindowVisible = !demoWindowVisible
            }
        }
    }

}