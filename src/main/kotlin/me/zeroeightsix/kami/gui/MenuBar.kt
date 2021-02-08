package me.zeroeightsix.kami.gui

import imgui.ImGui
import imgui.flag.ImGuiCol
import imgui.flag.ImGuiPopupFlags
import imgui.flag.ImGuiWindowFlags
import imgui.type.ImString
import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting
import java.util.UUID
import kotlin.reflect.KMutableProperty0
import me.zeroeightsix.kami.BaritoneIntegration
import me.zeroeightsix.kami.feature.FindSettings
import me.zeroeightsix.kami.gui.ImguiDSL.mainMenuBar
import me.zeroeightsix.kami.gui.ImguiDSL.menu
import me.zeroeightsix.kami.gui.ImguiDSL.menuItem
import me.zeroeightsix.kami.gui.widgets.EnabledWidgets
import me.zeroeightsix.kami.gui.widgets.GraphPinnableWidget
import me.zeroeightsix.kami.gui.widgets.InventoryPinnableWidget
import me.zeroeightsix.kami.gui.widgets.PinnableWidget
import me.zeroeightsix.kami.gui.widgets.PlayerPinnableWidget
import me.zeroeightsix.kami.gui.widgets.TextPinnableWidget
import me.zeroeightsix.kami.gui.windows.Settings
import me.zeroeightsix.kami.gui.windows.modules.ModuleWindowsEditor
import me.zeroeightsix.kami.gui.windows.modules.Modules

@FindSettings(settingsRoot = "view")
object View {
    @Setting
    var modulesOpen = true

    @Setting
    var consoleOpen = false

    @Setting
    var demoWindowVisible = false

    operator fun invoke() = menu("View") {
        fun toggleWindow(title: String, setting: KMutableProperty0<Boolean>, shortcut: String = "") =
            menuItem(title, shortcut, selected = setting()) { setting.set(!setting()) }

        toggleWindow("Settings", Settings::settingsWindowOpen)
        toggleWindow("Modules", ::modulesOpen)
        toggleWindow("Module window editor", ModuleWindowsEditor::open)
        if (Settings.demoWindowInView) toggleWindow("Demo window", ::demoWindowVisible)
    }
}

object MenuBar {

    private var buffer = ImString()
    private var errorText = ""
    private var widgetProducer: Pair<String, (String) -> Unit>? = null

    /**
     * Finds an unused [PinnableWidget.Position], or, if none, [PinnableWidget.Position.CUSTOM]
     */
    private fun findUnusedPosition() =
        PinnableWidget.Position.values()
            .toMutableSet()
            .also {
                it.removeAll(EnabledWidgets.widgets.map { it.position })
                it.remove(PinnableWidget.Position.CUSTOM)
            }
            .firstOrNull() ?: PinnableWidget.Position.CUSTOM

    operator fun invoke() = mainMenuBar {
        EnabledWidgets()
        View()
        menuCreate()
        BaritoneIntegration.menu()

        ImguiDSL.popupContextVoid("kami-void-popup", ImGuiPopupFlags.MouseButtonRight) {
            menuItem("Resize module windows") {
                Modules.resize = true
            }
            menuCreate()
            View()
        }
    }

    private fun menuCreate() {
        menu("Create") {
            menuItem("Text widget") {
                widgetProducer = "Create text widget" to { title ->
                    EnabledWidgets.textWidgets.add(
                        TextPinnableWidget(
                            title,
                            position = findUnusedPosition()
                        )
                    )
                }
            }
            menuItem("Player overlay") {
                widgetProducer = "Create player overlay" to { title ->
                    EnabledWidgets.playerWidgets.add(
                        PlayerPinnableWidget(
                            title,
                            position = findUnusedPosition()
                        )
                    )
                }
            }
            menuItem("Inventory overlay") {
                widgetProducer = "Create inventory overlay" to { title ->
                    EnabledWidgets.inventoryWidgets.add(
                        InventoryPinnableWidget(
                            title,
                            position = findUnusedPosition()
                        )
                    )
                }
            }
            menuItem("Graph") {
                widgetProducer = "Create graph" to { title ->
                    EnabledWidgets.graphs.add(
                        GraphPinnableWidget(
                            title,
                            position = findUnusedPosition()
                        )
                    )
                }
            }
        }

        val takenNames = EnabledWidgets.widgets.map { it.name }.toSet()
        widgetProducer?.let { (title, factory) ->
            ImGui.openPopup(title) // Calling this in the menu for some reason doesn't work. So we spam it instead, because ImGui handles this user error!
            ImguiDSL.popupModal(title, extraFlags = ImGuiWindowFlags.AlwaysAutoResize) {
                ImGui.inputText("Title", buffer)

                if (errorText.isNotBlank()) {
                    ImguiDSL.withStyleColour(ImGuiCol.Text, .7f, .3f, .3f, 1f) {
                        ImGui.text(errorText)
                    }
                }

                ImguiDSL.withStyleColour(ImGuiCol.Text, .7f, .7f, .7f, 1f) {
                    ImguiDSL.button("Cancel", 100f, 0f) {
                        errorText = ""
                        buffer.set("")
                        widgetProducer = null
                        ImGui.closeCurrentPopup()
                    }
                }
                ImGui.sameLine()
                ImguiDSL.button("Create", 100f, 0f) {
                    val widgetName = if (buffer.isEmpty) {
                        // TODO: imgui doesn't allow null or no title names
                        // bool ImGui::Begin(const char*, bool*, ImGuiWindowFlags): Assertion `name != __null && name[0] != '\0'' failed.
                        UUID.randomUUID().toString()
                    } else {
                        buffer.get()
                    }

                    if (!takenNames.contains(widgetName)) {
                        errorText = ""
                        buffer.set("")
                        factory(widgetName)
                        widgetProducer = null
                        ImGui.closeCurrentPopup()
                    } else {
                        errorText = "Title must be unique!"
                    }
                }
            }
        }
    }
}