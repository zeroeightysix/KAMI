package me.zeroeightsix.kami.gui.widgets

import imgui.ImGui.separator
import imgui.dsl.checkbox
import imgui.dsl.menu
import imgui.dsl.menuItem
import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting
import me.zeroeightsix.kami.feature.FindSettings

@FindSettings(settingsRoot = "clickGui")
object EnabledWidgets {

    var hideAll = false

    @Setting
    internal var widgets = Widgets(
        mutableListOf(
            Information,
            Coordinates,
            ActiveModules
        )
    )

    operator fun invoke() = menu("Overlay") {
        checkbox("Hide all", EnabledWidgets::hideAll) {}
        separator()
        enabledButtons()
        separator()
        menuItem("Pin all") {
            widgets.forEach {
                it.pinned = true
            }
        }
        menuItem("Unpin all") {
            widgets.forEach {
                it.pinned = false
            }
        }
    }

    fun enabledButtons() {
        for (widget in widgets) {
            menuItem(widget.name, "", widget.open, !hideAll) {
                widget.open = !widget.open
            }
        }
    }

    class Widgets(val widgets: MutableList<TextPinnableWidget>) : MutableList<TextPinnableWidget> by widgets {
        override fun equals(other: Any?): Boolean = false
        override fun hashCode(): Int {
            return widgets.hashCode()
        }
    }

}
