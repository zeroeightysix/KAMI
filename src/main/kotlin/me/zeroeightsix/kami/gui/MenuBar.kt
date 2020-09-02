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
import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting
import me.zeroeightsix.kami.backToString
import me.zeroeightsix.kami.feature.FindSettings
import me.zeroeightsix.kami.gui.widgets.EnabledWidgets
import me.zeroeightsix.kami.gui.widgets.PinnableWidget
import me.zeroeightsix.kami.gui.widgets.TextPinnableWidget
import me.zeroeightsix.kami.gui.windows.Settings
import me.zeroeightsix.kami.gui.windows.modules.ModuleWindowsEditor
import me.zeroeightsix.kami.gui.windows.modules.Modules

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

    private var buffer = ByteArray(128)

    operator fun invoke() = mainMenuBar {
        EnabledWidgets()
        View()

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
            View()
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
                EnabledWidgets.textWidgets.add(
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
