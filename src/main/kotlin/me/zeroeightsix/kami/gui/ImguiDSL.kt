package me.zeroeightsix.kami.gui

import imgui.ImGui
import imgui.ImGui.beginChild
import imgui.ImGui.beginMenu
import imgui.ImGui.button
import imgui.ImGui.calcTextSize
import imgui.ImGui.endMenu
import imgui.ImGui.getWindowContentRegionMaxX
import imgui.ImGui.getWindowContentRegionMinX
import imgui.ImGui.menuItem
import imgui.ImGui.popItemWidth
import imgui.ImGui.popStyleColor
import imgui.ImGui.pushItemWidth
import imgui.ImGui.pushStyleColor
import imgui.ImGuiStyle
import imgui.ImVec2
import imgui.flag.ImGuiCol
import imgui.type.ImBoolean
import imgui.type.ImString
import kotlin.reflect.KMutableProperty0

typealias WindowFlags = Int

object ImguiDSL {
    inline fun window(name: String, flags: WindowFlags = 0, block: () -> Unit) =
        window(name, ImBoolean(true), flags, block)

    inline fun window(name: String, open: KMutableProperty0<Boolean>, flags: WindowFlags = 0, block: () -> Unit) =
        wrapImBool(open) {
            window(name, it, flags, block)
        }

    inline fun window(name: String, open: ImBoolean, flags: WindowFlags = 0, block: () -> Unit) {
        if (ImGui.begin(name, open, flags)) // ~open
            try {
                block()
            } finally {
                ImGui.end()
            }
        else
            ImGui.end()
    }

    inline fun child(
        strId: String,
        width: Float = 0f,
        height: Float = 0f,
        border: Boolean = false,
        extraFlags: WindowFlags = 0,
        block: () -> Unit
    ) {
        if (beginChild(strId, width, height, border, extraFlags)) // ~open
            try {
                block()
            } finally {
                ImGui.endChild()
            }
        else
            ImGui.endChild()
    }

    inline fun button(label: String, width: Float = 0f, height: Float = 0f, block: () -> Unit) {
        if (button(label, width, height))
            block()
    }

    inline fun mainMenuBar(block: () -> Unit) {
        if (ImGui.beginMainMenuBar())
            try {
                block()
            } finally {
                ImGui.endMainMenuBar()
            }
    }

    inline fun menu(label: String, enabled: Boolean = true, block: () -> Unit) {
        if (beginMenu(label, enabled))
            try {
                block()
            } finally {
                endMenu()
            }
    }

    inline fun menuItem(
        label: String,
        shortcut: String = "",
        selected: Boolean = false,
        enabled: Boolean = true,
        block: () -> Unit
    ) {
        if (menuItem(label, shortcut, selected, enabled))
            block()
    }

    inline fun withItemWidth(itemWidth: Int, block: () -> Unit): Unit =
        withItemWidth(itemWidth.toFloat(), block)

    inline fun withItemWidth(itemWidth: Float, block: () -> Unit) {
        pushItemWidth(itemWidth)
        try {
            block()
        } finally {
            popItemWidth()
        }
    }

    inline fun withStyleColor(idx: Int, col: Int, block: () -> Unit) {
        pushStyleColor(idx, col)
        block()
        popStyleColor()
    }

    inline fun withStyleColor(idx: Int, red: Float, green: Float, blue: Float, alpha: Float, block: () -> Unit) {
        pushStyleColor(idx, red, green, blue, alpha)
        block()
        popStyleColor()
    }

    inline fun withStyleColor(idx: Int, red: Int, green: Int, blue: Int, alpha: Int, block: () -> Unit) {
        pushStyleColor(idx, red, green, blue, alpha)
        block()
        popStyleColor()
    }

    inline fun calcTextSize(str: String) = calcTextSize(str, ImGui::calcTextSize)

    inline fun calcTextSize(str: String, calc: (ImVec2, String) -> Unit): ImVec2 {
        val dst = ImVec2(0f, 0f)
        calc(dst, str)
        return dst
    }

    inline fun wrapImBool(property: KMutableProperty0<Boolean>, block: (ImBoolean) -> Unit) {
        val bool = ImBoolean(property())
        block(bool)
        property.set(bool.get())
    }

    inline infix fun Int.without(other: Int) = this and other.inv()

    val String.imgui: ImString
        get() = ImString(this)

    val windowContentRegionWidth: Float
        get() = getWindowContentRegionMaxX() - getWindowContentRegionMinX()

    var cursorPosX: Float
        get() = ImGui.getCursorPosX()
        set(value) = ImGui.setCursorPosX(value)

    var cursorPosY: Float
        get() = ImGui.getCursorPosY()
        set(value) = ImGui.setCursorPosY(value)

    var ImGuiStyle.colors: Array<FloatArray>
        get() {
            val buf = Array(ImGuiCol.COUNT) { FloatArray(4) }
            this.getColors(buf)
            return buf
        }
        set(value) = this.setColors(value)
}
