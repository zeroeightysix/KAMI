package me.zeroeightsix.kami.gui

import imgui.dsl.mainMenuBar
import imgui.dsl.menu
import imgui.dsl.menuItem
import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting
import me.zeroeightsix.kami.BaritoneIntegration
import me.zeroeightsix.kami.feature.FindSettings
import me.zeroeightsix.kami.gui.widgets.EnabledWidgets
import me.zeroeightsix.kami.gui.widgets.VoidContextMenu
import me.zeroeightsix.kami.gui.windows.Settings
import me.zeroeightsix.kami.gui.windows.modules.ModuleWindowsEditor

@FindSettings(settingsRoot = "view")
object View {
    @Setting
    var modulesOpen = true

    @Setting
    var demoWindowVisible = false

    operator fun invoke() = menu("View") {
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
}

object MenuBar {

    operator fun invoke() = mainMenuBar {
        EnabledWidgets()
        View()
        BaritoneIntegration.menu()

        // imgui needs a window to add the void popup to, so we disgustingly add it to the only window that will always be there: the main menu bar
        VoidContextMenu()
    }

}
