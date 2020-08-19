package me.zeroeightsix.kami.gui

import imgui.MouseButton
import imgui.dsl.mainMenuBar
import imgui.dsl.menu
import imgui.dsl.menuItem
import imgui.dsl.popupContextVoid
import me.zeroeightsix.kami.gui.View.demoWindowVisible
import me.zeroeightsix.kami.gui.View.modulesOpen
import me.zeroeightsix.kami.gui.widgets.EnabledWidgets
import me.zeroeightsix.kami.gui.windows.Settings
import me.zeroeightsix.kami.gui.windows.modules.ModuleWindowsEditor
import me.zeroeightsix.kami.gui.windows.modules.Modules

object View {
    var modulesOpen = true
    var demoWindowVisible = false
}

object MenuBar {

    operator fun invoke() = mainMenuBar {
        EnabledWidgets()
        menu("View") {
            menuItem("Settings", "", selected = Settings.settingsWindowOpen) {
                Settings.settingsWindowOpen = !Settings.settingsWindowOpen
            }
            menuItem("Modules", "", selected = modulesOpen) {
                modulesOpen = !modulesOpen
            }
            menuItem("Module window editor", "", selected = ModuleWindowsEditor.open) {
                ModuleWindowsEditor.open = !ModuleWindowsEditor.open
            }
            if (Settings.demoWindowVisible) {
                menuItem("Demo window", "", selected = demoWindowVisible) {
                    demoWindowVisible = !demoWindowVisible
                }
            }
        }

        // imgui needs a window to add the void popup to, so we disgustingly add it to the only window that will always be there: the main menu bar
        popupContextVoid("kami-void-popup", MouseButton.Right) {
            menuItem("Resize module windows") {
                Modules.resize = true
            }
        }
    }

}
