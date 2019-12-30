package me.zeroeightsix.kami.gui.widgets

import glm_.vec2.Vec2
import imgui.Cond
import imgui.ImGui.io
import imgui.ImGui.setNextWindowPos
import imgui.WindowFlag
import imgui.dsl.menu
import imgui.dsl.menuItem
import imgui.dsl.popupContextWindow
import imgui.dsl.window
import imgui.or
import kotlin.reflect.KMutableProperty0

abstract class PinnableWidget(val name: String) {

    var pinned = false
    var corner = 0

    val distance = 10f

    private fun showWidgetContextMenu(open: KMutableProperty0<Boolean>) {
        popupContextWindow {
            menuItem("Pinned", "", pinned) {
                pinned = !pinned
            }
            menu("Position") {
                // Shamelessly stolen from SimpleOverlay
                menuItem("Custom", "", corner == -1) { corner = -1 }
                menuItem("Top-left", "", corner == 0) { corner = 0 }
                menuItem("Top-right", "", corner == 1) { corner = 1 }
                menuItem("Bottom-left", "", corner == 2) { corner = 2 }
                menuItem("Bottom-right", "", corner == 3) { corner = 3 }
            }
            fillContextMenu()
            menuItem("Close", "CTRL+W") {
                open.set(false)
            }
        }
    }

    fun showWindow(open: KMutableProperty0<Boolean>) {
        var flags = WindowFlag.NoDecoration or WindowFlag.AlwaysAutoResize or WindowFlag.NoSavedSettings or WindowFlag.NoFocusOnAppearing or WindowFlag.NoNav
        if (corner != -1) {
            val windowPos = Vec2{ if (corner has it + 1) io.displaySize[it] - distance else distance }
            val windowPosPivot = Vec2(if (corner has 1) 1f else 0f, if (corner has 2) 1f else 0f)
            setNextWindowPos(windowPos, Cond.Always, windowPosPivot)
            flags = flags or WindowFlag.NoMove
        }

        window(name, open, flags) {
            fillWindow(open)
            showWidgetContextMenu(open)
        }
    }

    private infix fun Int.has(b: Int) = (this and b) != 0

    protected abstract fun fillWindow(open: KMutableProperty0<Boolean>)
    protected open fun fillContextMenu() {}

}