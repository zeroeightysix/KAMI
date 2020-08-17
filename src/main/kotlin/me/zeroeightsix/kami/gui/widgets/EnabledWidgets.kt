package me.zeroeightsix.kami.gui.widgets

import imgui.ImGui.separator
import imgui.dsl.checkbox
import imgui.dsl.menu
import imgui.dsl.menuItem
import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting

object EnabledWidgets {

    var hideAll = false

    @Setting
    internal var widgets = mutableMapOf(
        Information to true,
        Coordinates to true,
        ActiveModules to true
    )

    operator fun invoke() = menu("Overlay") {
        checkbox("Hide all", EnabledWidgets::hideAll) {}
        separator()
        enabledButtons()
        separator()
        menuItem("Pin all") {
            widgets.keys.forEach {
                it.pinned = true
            }
        }
        menuItem("Unpin all") {
            widgets.keys.forEach {
                it.pinned = false
            }
        }
    }

    fun enabledButtons() {
        for ((widget, open) in widgets) {
            menuItem(widget.name, "", open, !hideAll) {
                widgets[widget] = !open
            }
        }
    }

}
