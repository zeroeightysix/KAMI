package me.zeroeightsix.kami.gui.widgets

import glm_.vec2.Vec2
import imgui.Cond
import imgui.ImGui
import imgui.ImGui.begin
import imgui.ImGui.end
import imgui.ImGui.io
import imgui.ImGui.setNextWindowBgAlpha
import imgui.ImGui.setNextWindowPos
import imgui.ImGuiWindowFlags
import imgui.api.g
import imgui.dsl.menu
import imgui.dsl.menuItem
import imgui.dsl.popupContextWindow
import imgui.or
import me.zeroeightsix.kami.gui.windows.Settings

abstract class PinnableWidget(
    val name: String,
    var position: Position = Position.TOP_LEFT,
    var open: Boolean = true,
    var pinned: Boolean = true,
    var background: Boolean = false
) {
    protected var autoResize = true

    companion object {
        var drawFadedBackground = true
    }

    private fun showWidgetContextMenu(): Boolean {
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
            menuItem("Delete") {
                return true
            }
            fillContextMenu()
        }
        return false
    }

    /**
     * @return `true` if this widget should be removed
     */
    fun showWindow(limitY: Boolean): Boolean {
        preWindow()

        var flags =
            ImGuiWindowFlags.NoDecoration or ImGuiWindowFlags.NoFocusOnAppearing or ImGuiWindowFlags.NoNav
        if (autoResize) flags = flags or ImGuiWindowFlags.AlwaysAutoResize
        else if (drawFadedBackground) flags = flags xor ImGuiWindowFlags.NoResize

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
            flags = flags or ImGuiWindowFlags.NoMove
        }

        if (!background) {
            if (drawFadedBackground) {
                setNextWindowBgAlpha(0.45f)
            } else flags = flags or ImGuiWindowFlags.NoBackground
        }

        if (begin(name, ::open, flags)) {
            fillWindow()
            if (showWidgetContextMenu()) return true

            end()
        }

        postWindow()
        return false
    }

    private infix fun Int.has(b: Int) = (this and b) != 0

    protected abstract fun fillWindow()
    protected open fun fillStyle() {}
    protected open fun fillContextMenu() {}
    protected open fun preWindow() {}
    protected open fun postWindow() {}

    public enum class Position(val top: Boolean, val left: Boolean) {
        CUSTOM(false, false), TOP_LEFT(true, true), TOP_RIGHT(true, false), BOTTOM_LEFT(false, true), BOTTOM_RIGHT(false, false)
    }
}
