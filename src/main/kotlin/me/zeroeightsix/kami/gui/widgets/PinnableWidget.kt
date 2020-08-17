package me.zeroeightsix.kami.gui.widgets

import glm_.vec2.Vec2
import imgui.Cond
import imgui.ImGui
import imgui.ImGui.begin
import imgui.ImGui.end
import imgui.ImGui.io
import imgui.ImGui.setNextWindowBgAlpha
import imgui.ImGui.setNextWindowPos
import imgui.WindowFlag
import imgui.api.g
import imgui.dsl.menu
import imgui.dsl.menuItem
import imgui.dsl.popupContextWindow
import imgui.or
import me.zeroeightsix.kami.gui.windows.Settings

abstract class PinnableWidget(val name: String, private var position: Position = Position.TOP_LEFT) {

    var pinned = true
    var background = false

    companion object {
        var drawFadedBackground = true
    }

    private fun showWidgetContextMenu(open: BooleanArray) {
        popupContextWindow {
            menuItem("Pinned", "", pinned) {
                pinned = !pinned
            }
            menu("Position") {
                // Shamelessly stolen from SimpleOverlay
                menuItem("Custom", "", position == Position.CUSTOM) { position = Position.CUSTOM }
                menuItem("Top-left", "", position == Position.TOP_LEFT) { position = Position.TOP_LEFT }
                menuItem("Top-right", "", position == Position.TOP_RIGHT) { position = Position.TOP_RIGHT }
                menuItem("Bottom-left", "", position == Position.BOTTOM_LEFT) { position = Position.BOTTOM_LEFT }
                menuItem("Bottom-right", "", position == Position.BOTTOM_RIGHT) { position = Position.BOTTOM_RIGHT }
            }
            menu("Style") {
                menuItem("Background", "", background) { background = !background }
                fillStyle()
            }
            fillContextMenu()
        }
    }

    /**
     * @return the possibly modified 'open' attribute
     */
    fun showWindow(open: Boolean, limitY: Boolean): Boolean {
        preWindow()

        var flags =
            WindowFlag.NoDecoration or WindowFlag.AlwaysAutoResize or WindowFlag.NoSavedSettings or WindowFlag.NoFocusOnAppearing or WindowFlag.NoNav
        if (position != Position.CUSTOM) {
            // TODO: Move windows when the main menu bar is shown or when chat is opened
            val distance = Settings.borderOffset
            val topDistance =
                if (limitY) distance.coerceAtLeast(g.nextWindowData.menuBarOffsetMinVal.y + g.fontBaseSize + ImGui.style.framePadding.y + 4) else distance
            val windowPos = Vec2(
                if (position.left) distance else io.displaySize[0] - distance,
                if (position.top) topDistance else io.displaySize[1] - distance
            )
            val windowPosPivot = Vec2(if (position.left) 0 else 1, if (position.top) 0 else 1)
            setNextWindowPos(windowPos, Cond.Always, windowPosPivot)
            flags = flags or WindowFlag.NoMove
        }

        if (!background) {
            if (drawFadedBackground) {
                setNextWindowBgAlpha(0.45f)
            } else flags = flags or WindowFlag.NoBackground
        }

        val openArray = booleanArrayOf(open)
        if (begin(name, openArray, flags)) {
            fillWindow(openArray)
            showWidgetContextMenu(openArray)

            end()
        }

        return openArray[0]
    }

    private infix fun Int.has(b: Int) = (this and b) != 0

    protected abstract fun fillWindow(open: BooleanArray)
    protected open fun fillStyle() {}
    protected open fun fillContextMenu() {}
    protected open fun preWindow() {}

    public enum class Position(val top: Boolean, val left: Boolean) {
        CUSTOM(false, false), TOP_LEFT(true, true), TOP_RIGHT(true, false), BOTTOM_LEFT(false, true), BOTTOM_RIGHT(false, false)
    }

}
