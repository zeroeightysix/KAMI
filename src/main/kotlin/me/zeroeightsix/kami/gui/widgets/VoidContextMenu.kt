package me.zeroeightsix.kami.gui.widgets

import glm_.vec2.Vec2
import glm_.vec4.Vec4
import imgui.Col
import imgui.ImGui
import imgui.MouseButton
import imgui.ImGuiWindowFlags
import imgui.cStr
import imgui.dsl
import me.zeroeightsix.kami.gui.View
import me.zeroeightsix.kami.gui.windows.modules.Modules

/**
 * The context menu that appears when the user right-clicks where there are no imgui windows
 */
object VoidContextMenu {

    private var buffer = ByteArray(128)
    private var widgetProducer: Pair<String, (String) -> Unit>? = null

    operator fun invoke() {
        dsl.popupContextVoid("kami-void-popup", MouseButton.Right) {
            dsl.menuItem("Resize module windows") {
                Modules.resize = true
            }
            dsl.menu("Create") {
                dsl.menuItem("Text widget") {
                    widgetProducer = "Create text widget" to { title ->
                        EnabledWidgets.textWidgets.add(
                            TextPinnableWidget(
                                title,
                                position = findUnusedPosition()
                            )
                        )
                    }
                }
                dsl.menuItem("Player overlay") {
                    widgetProducer = "Create player overlay" to { title ->
                        EnabledWidgets.playerWidgets.add(
                            PlayerPinnableWidget(
                                title,
                                position = findUnusedPosition()
                            )
                        )
                    }
                }
                dsl.menuItem("Inventory overlay") {
                    widgetProducer = "Create inventory overlay" to { title ->
                        EnabledWidgets.inventoryWidgets.add(
                            InventoryPinnableWidget(
                                title,
                                position = findUnusedPosition()
                            )
                        )
                    }
                }
                dsl.menuItem("Graph") {
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
            dsl.popupModal(title, extraFlags = ImGuiWindowFlags.AlwaysAutoResize) {
                ImGui.inputText("Title", buffer)

                ImGui.pushStyleColor(Col.Text, Vec4(.7f, .7f, .7f, 1f))
                dsl.button("Cancel", Vec2(100, 0)) {
                    widgetProducer = null
                    ImGui.closeCurrentPopup()
                }
                ImGui.popStyleColor()
                ImGui.sameLine()
                dsl.button("Create", Vec2(100, 0)) {
                    val widgetName = buffer.cStr
                    buffer = ByteArray(128)
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
