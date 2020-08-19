package me.zeroeightsix.kami.gui

import glm_.vec2.Vec2
import glm_.vec4.Vec4
import imgui.Col
import imgui.ImGui
import imgui.MouseButton
import imgui.WindowFlag
import imgui.dsl.button
import imgui.dsl.mainMenuBar
import imgui.dsl.menu
import imgui.dsl.menuItem
import imgui.dsl.popupContextVoid
import imgui.dsl.popupModal
import me.zeroeightsix.kami.backToString
import me.zeroeightsix.kami.gui.View.demoWindowVisible
import me.zeroeightsix.kami.gui.View.modulesOpen
import me.zeroeightsix.kami.gui.widgets.EnabledWidgets
import me.zeroeightsix.kami.gui.widgets.PinnableWidget
import me.zeroeightsix.kami.gui.widgets.TextPinnableWidget
import me.zeroeightsix.kami.gui.windows.Settings
import me.zeroeightsix.kami.gui.windows.modules.ModuleWindowsEditor
import me.zeroeightsix.kami.gui.windows.modules.Modules

object View {
    var modulesOpen = true
    var demoWindowVisible = false
}

object MenuBar {

    private var buffer = ByteArray(128)

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
        var openModal = false
        popupContextVoid("kami-void-popup", MouseButton.Right) {
            menuItem("Resize module windows") {
                Modules.resize = true
            }
            menu("Create") {
                menuItem("Text widget") {
                    openModal = true
                }
            }
        }
        if (openModal) {
            ImGui.openPopup("Create text widget")
        }

        popupModal("Create text widget", extraFlags = WindowFlag.AlwaysAutoResize.i) {
            ImGui.inputText("Title", buffer)

            ImGui.pushStyleColor(Col.Text, Vec4(.7f, .7f, .7f, 1f))
            button("Cancel", Vec2(100, 0)) {
                ImGui.closeCurrentPopup()
            }
            ImGui.popStyleColor()
            ImGui.sameLine()
            button("Create", Vec2(100, 0)) {
                val title = buffer.backToString()
                buffer = ByteArray(128)
                EnabledWidgets.widgets.add(
                    TextPinnableWidget(
                        title,
                        // Find an unused position, or, if none, pick CUSTOM.
                        position = PinnableWidget.Position.values()
                            .toMutableSet()
                            .also {
                                it.removeAll(EnabledWidgets.widgets.map { it.position })
                                it.remove(PinnableWidget.Position.CUSTOM)
                            }
                            .firstOrNull() ?: PinnableWidget.Position.CUSTOM
                    )
                )
                ImGui.closeCurrentPopup()
            }
        }
    }

}
