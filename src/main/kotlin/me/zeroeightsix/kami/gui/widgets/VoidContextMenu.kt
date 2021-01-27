package me.zeroeightsix.kami.gui.widgets

import imgui.ImGui
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

/**
 * The context menu that appears when the user right-clicks where there are no imgui windows
 */
object VoidContextMenu {

    private var buffer = ImString()
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

        widgetProducer?.let { (title, factory) ->
            ImGui.openPopup(title) // Calling this in the menu for some reason doesn't work. So we spam it instead, because ImGui handles this user error!
            popupModal(title, extraFlags = ImGuiWindowFlags.AlwaysAutoResize) {
                ImGui.inputText("Title", buffer)

                withStyleColour(ImGuiCol.Text, .7f, .7f, .7f, 1f) {
                    button("Cancel", 100f, 0f) {
                        widgetProducer = null
                        ImGui.closeCurrentPopup()
                    }
                }
                ImGui.sameLine()
                button("Create", 100f, 0f) {
                    val widgetName = buffer.get() ?: ""
                    buffer.set("")
                    factory(widgetName)
                    widgetProducer = null
                    ImGui.closeCurrentPopup()
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
