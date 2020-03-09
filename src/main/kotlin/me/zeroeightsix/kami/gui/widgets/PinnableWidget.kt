package me.zeroeightsix.kami.gui.widgets

import glm_.vec2.Vec2
import imgui.Cond
import imgui.ImGui
import imgui.ImGui.io
import imgui.ImGui.setNextWindowBgAlpha
import imgui.ImGui.setNextWindowPos
import imgui.WindowFlag
import imgui.api.g
import imgui.dsl.menu
import imgui.dsl.menuItem
import imgui.dsl.popupContextWindow
import imgui.dsl.window
import imgui.or
import me.zeroeightsix.kami.gui.KamiGuiScreen
import me.zeroeightsix.kami.gui.windows.KamiSettings
import me.zeroeightsix.kami.util.Wrapper
import kotlin.reflect.KMutableProperty0

abstract class PinnableWidget(val name: String, private var position: Position = Position.TOP_LEFT) {

    var pinned = false
    var background = false

    companion object {
        var drawFadedBackground = true
    }

    private fun showWidgetContextMenu(open: KMutableProperty0<Boolean>) {
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
            menuItem("Hide", "Ctrl+W") {
                open.set(false)
            }
        }
    }

    fun showWindow(open: KMutableProperty0<Boolean>) {
        preWindow()

        var flags = WindowFlag.NoDecoration or WindowFlag.AlwaysAutoResize or WindowFlag.NoSavedSettings or WindowFlag.NoFocusOnAppearing or WindowFlag.NoNav
        if (position != Position.CUSTOM) {
            // TODO: Move windows when the main menu bar is shown or when chat is opened
            val distance = KamiSettings.borderOffset
            val topDistance = if (Wrapper.getMinecraft().currentScreen is KamiGuiScreen) distance.coerceAtLeast(g.nextWindowData.menuBarOffsetMinVal.y + g.fontBaseSize + ImGui.style.framePadding.y + 4) else distance
            val windowPos = Vec2(if (position.left) distance else io.displaySize[0] - distance, if (position.top) topDistance else io.displaySize[1] - distance)
            val windowPosPivot = Vec2(if (position.left) 0 else 1, if (position.top) 0 else 1)
            setNextWindowPos(windowPos, Cond.Always, windowPosPivot)
            flags = flags or WindowFlag.NoMove
        }

        if (!background) {
            if (drawFadedBackground) {
                setNextWindowBgAlpha(0.45f)
            } else flags = flags or WindowFlag.NoBackground
        }

        window(name, open, flags) {
            fillWindow(open)
            showWidgetContextMenu(open)
        }
    }

    private infix fun Int.has(b: Int) = (this and b) != 0

    protected abstract fun fillWindow(open: KMutableProperty0<Boolean>)
    protected open fun fillStyle() {}
    protected open fun fillContextMenu() {}
    protected open fun preWindow() {}

    public enum class Position(val top: Boolean, val left: Boolean) {
        CUSTOM(false, false), TOP_LEFT(true, true), TOP_RIGHT(true, false), BOTTOM_LEFT(false, true), BOTTOM_RIGHT(false, false)
    }

}