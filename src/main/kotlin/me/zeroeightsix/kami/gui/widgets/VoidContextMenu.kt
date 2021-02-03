package me.zeroeightsix.kami.gui.widgets

import imgui.ImGui
import imgui.ImGui.text
import imgui.flag.ImGuiCol
import imgui.flag.ImGuiPopupFlags
import imgui.flag.ImGuiWindowFlags
import imgui.type.ImString
import me.zeroeightsix.kami.gui.ImguiDSL.button
import me.zeroeightsix.kami.gui.ImguiDSL.menu
import me.zeroeightsix.kami.gui.ImguiDSL.menuItem
import me.zeroeightsix.kami.gui.ImguiDSL.popupContextVoid
import me.zeroeightsix.kami.gui.ImguiDSL.popupModal
import me.zeroeightsix.kami.gui.ImguiDSL.withStyleColour
import me.zeroeightsix.kami.gui.View
import me.zeroeightsix.kami.gui.windows.modules.Modules
import java.util.*

/**
 * The context menu that appears when the user right-clicks where there are no imgui windows
 */
object VoidContextMenu {

    private var buffer = ImString()
    private var errorText = ""
    private var widgetProducer: Pair<String, (String) -> Unit>? = null

    operator fun invoke() {
        popupContextVoid("kami-void-popup", ImGuiPopupFlags.MouseButtonRight) {
            menuItem("Resize module windows") {
                Modules.resize = true
            }
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
            View()
        }

        val takenNames = EnabledWidgets.widgets.map { it.name }.toSet()
        widgetProducer?.let { (title, factory) ->
            ImGui.openPopup(title) // Calling this in the menu for some reason doesn't work. So we spam it instead, because ImGui handles this user error!
            popupModal(title, extraFlags = ImGuiWindowFlags.AlwaysAutoResize) {
                ImGui.inputText("Title", buffer)

                if (errorText.isNotBlank()) {
                    withStyleColour(ImGuiCol.Text, .7f, .3f, .3f, 1f) {
                        text(errorText)
                    }
                }

                withStyleColour(ImGuiCol.Text, .7f, .7f, .7f, 1f) {
                    button("Cancel", 100f, 0f) {
                        errorText = ""
                        buffer.set("")
                        widgetProducer = null
                        ImGui.closeCurrentPopup()
                    }
                }
                ImGui.sameLine()
                button("Create", 100f, 0f) {
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
}
