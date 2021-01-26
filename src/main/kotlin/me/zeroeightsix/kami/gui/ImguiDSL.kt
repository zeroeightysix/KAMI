package me.zeroeightsix.kami.gui

import imgui.ImGui
import imgui.ImGui.beginChild
import imgui.ImGui.beginMenu
import imgui.ImGui.beginPopup
import imgui.ImGui.beginPopupContextItem
import imgui.ImGui.beginPopupContextVoid
import imgui.ImGui.beginPopupContextWindow
import imgui.ImGui.beginPopupModal
import imgui.ImGui.beginTabBar
import imgui.ImGui.beginTabItem
import imgui.ImGui.button
import imgui.ImGui.endMenu
import imgui.ImGui.endPopup
import imgui.ImGui.endTabBar
import imgui.ImGui.endTabItem
import imgui.ImGui.getFontSize
import imgui.ImGui.getWindowContentRegionMaxX
import imgui.ImGui.getWindowContentRegionMinX
import imgui.ImGui.menuItem
import imgui.ImGui.popID
import imgui.ImGui.popItemWidth
import imgui.ImGui.popStyleColor
import imgui.ImGui.popStyleVar
import imgui.ImGui.popTextWrapPos
import imgui.ImGui.pushID
import imgui.ImGui.pushItemWidth
import imgui.ImGui.pushStyleColor
import imgui.ImGui.pushStyleVar
import imgui.ImGui.pushTextWrapPos
import imgui.ImGui.radioButton
import imgui.ImGui.textDisabled
import imgui.ImGui.textUnformatted
import imgui.ImGuiStyle
import imgui.ImVec2
import imgui.flag.ImGuiCol
import imgui.flag.ImGuiPopupFlags
import imgui.type.ImBoolean
import imgui.type.ImFloat
import imgui.type.ImInt
import imgui.type.ImString
import kotlin.reflect.KMutableProperty0
import me.zeroeightsix.kami.Colour

typealias WindowFlags = Int

object ImguiDSL {
    const val PAYLOAD_TYPE_COLOR_4F = "_COL4F"

    inline fun window(name: String, open: KMutableProperty0<Boolean>, flags: WindowFlags = 0, block: () -> Unit) =
        wrapImBool(open) {
            window(name, it, flags, block)
        }

    inline fun window(name: String, open: ImBoolean? = null, flags: WindowFlags = 0, block: () -> Unit) {
        if (if (open != null) { ImGui.begin(name, open, flags) } else { ImGui.begin(name, flags) })
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

    inline fun checkbox(label: String, bool: KMutableProperty0<Boolean>, block: () -> Unit = {}) = wrapImBool(bool) {
        checkbox(label, it, block)
    }

    inline fun checkbox(label: String, bool: ImBoolean, block: () -> Unit = {}) {
        if (ImGui.checkbox(label, bool)) {
            block()
        }
    }

    inline fun button(label: String, width: Float = 0f, height: Float = 0f, block: () -> Unit) {
        if (button(label, width, height))
            block()
    }

    inline fun combo(
        label: String,
        currentItem: ImInt,
        item: String,
        heightInItems: Int = -1,
        block: () -> Unit
    ) {
        if (ImGui.combo(label, currentItem, item, heightInItems))
            block()
    }

    inline fun combo(
        label: String,
        currentItem: ImInt,
        items: Array<String>,
        heightInItems: Int = -1,
        block: (ImInt) -> Unit
    ) {
        if (ImGui.combo(label, currentItem, items, items.size, heightInItems))
            block(currentItem)
    }

    inline fun combo(
        label: String,
        currentItem: ImInt,
        items: Collection<String>,
        heightInItems: Int = -1,
        block: (ImInt) -> Unit
    ) {
        combo(label, currentItem, items.toTypedArray(), heightInItems, block)
    }

    inline fun combo(
        label: String,
        currentItem: KMutableProperty0<Int>,
        item: String,
        heightInItems: Int = -1,
        block: () -> Unit
    ) {
        if (wrapImInt(currentItem) { ImGui.combo(label, it, item, heightInItems) })
            block()
    }

    inline fun combo(
        label: String,
        currentItem: KMutableProperty0<Int>,
        items: Array<String>,
        heightInItems: Int = -1,
        block: (ImInt) -> Unit
    ) {
        wrapImInt(currentItem) {
            combo(label, it, items, heightInItems, block)
        }
    }

    inline fun combo(
        label: String,
        currentItem: KMutableProperty0<Int>,
        items: Collection<String>,
        heightInItems: Int = -1,
        block: (ImInt) -> Unit
    ) {
        combo(label, currentItem, items.toTypedArray(), heightInItems, block)
    }

    inline fun mainMenuBar(block: () -> Unit) {
        if (ImGui.beginMainMenuBar())
            try {
                block()
            } finally {
                ImGui.endMainMenuBar()
            }
    }

    inline fun menuBar(block: () -> Unit) {
        if (ImGui.beginMenuBar()) {
            try {
                block()
            } finally {
                ImGui.endMenuBar()
            }
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

    inline fun popup(
        strId: String,
        popupFlags: Int = 0,
        block: () -> Unit
    ) {
        if (beginPopup(strId, popupFlags)) {
            try {
                block()
            } finally {
                endPopup()
            }
        }
    }

    inline fun popupContextItem(
        strId: String = "",
        popupFlags: Int = ImGuiPopupFlags.MouseButtonRight,
        block: () -> Unit
    ) {
        if (beginPopupContextItem(strId, popupFlags)) {
            try {
                block()
            } finally {
                endPopup()
            }
        }
    }

    inline fun popupContextWindow(
        strId: String = "",
        popupFlags: Int = ImGuiPopupFlags.MouseButtonRight,
        block: () -> Unit
    ) {
        if (beginPopupContextWindow(strId, popupFlags)) {
            try {
                block()
            } finally {
                endPopup()
            }
        }
    }

    inline fun popupContextVoid(
        strId: String = "",
        popupFlags: Int = ImGuiPopupFlags.MouseButtonRight,
        block: () -> Unit
    ) {
        if (beginPopupContextVoid(strId, popupFlags)) {
            try {
                block()
            } finally {
                endPopup()
            }
        }
    }

    inline fun popupModal(
        title: String,
        pOpen: KMutableProperty0<Boolean>? = null,
        extraFlags: Int = 0,
        block: () -> Unit
    ) {
        val beginRet = if (pOpen != null) {
            wrapImBool(pOpen) { beginPopupModal(title, it, extraFlags) }
        } else {
            beginPopupModal(title, extraFlags)
        }
        if (beginRet) {
            try {
                block()
            } finally {
                endPopup()
            }
        }
    }

    inline fun radioButton(
        label: String,
        active: Boolean,
        block: () -> Unit
    ) {
        if (radioButton(label, active)) {
            block()
        }
    }

    inline fun tabBar(
        strId: String,
        flags: Int = 0,
        block: () -> Unit
    ) {
        if (beginTabBar(strId, flags)) {
            try {
                block()
            } finally {
                endTabBar()
            }
        }
    }

    inline fun tabItem(
        label: String,
        pOpen: KMutableProperty0<Boolean>? = null,
        flags: Int = 0,
        block: () -> Unit
    ) {
        val beginRet = if (pOpen != null) {
            wrapImBool(pOpen) { beginTabItem(label, it, flags) }
        } else {
            beginTabItem(label, flags)
        }
        if (beginRet) {
            try {
                block()
            } finally {
                endTabItem()
            }
        }
    }

    inline fun dragDropSource(flags: Int = 0, block: () -> Unit) {
        if (ImGui.beginDragDropSource(flags))
            try {
                block()
            } finally {
                ImGui.endDragDropSource()
            }
    }

    inline fun dragDropTarget(block: () -> Unit) {
        if (ImGui.beginDragDropTarget())
            try {
                block()
            } finally {
                ImGui.endDragDropTarget()
            }
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

    inline fun withId(id: Int, block: () -> Unit) {
        pushID(id)
        try {
            block()
        } finally {
            popID()
        }
    }

    inline fun withId(id: String, block: () -> Unit) {
        pushID(id)
        try {
            block()
        } finally {
            popID()
        }
    }

    inline fun <E : Enum<E>> withId(id: E, block: () -> Unit) {
        pushID(id.ordinal)
        try {
            block()
        } finally {
            popID()
        }
    }

    inline fun withStyleColour(idx: Int, col: Int, block: () -> Unit) {
        pushStyleColor(idx, col)
        try {
            block()
        } finally {
            popStyleColor()
        }
    }

    inline fun withStyleColour(idx: Int, colour: Colour, block: () -> Unit) {
        withStyleColour(idx, colour.asFloatRGBA(), block)
    }

    inline fun withStyleColour(idx: Int, red: Float, green: Float, blue: Float, alpha: Float, block: () -> Unit) {
        pushStyleColor(idx, red, green, blue, alpha)
        try {
            block()
        } finally {
            popStyleColor()
        }
    }

    inline fun withStyleColour(idx: Int, colour: FloatArray, block: () -> Unit) {
        pushStyleColor(idx, colour[0], colour[1], colour[2], colour[3])
        try {
            block()
        } finally {
            popStyleColor()
        }
    }

    inline fun withStyleColour(idx: Int, red: Int, green: Int, blue: Int, alpha: Int, block: () -> Unit) {
        pushStyleColor(idx, red, green, blue, alpha)
        try {
            block()
        } finally {
            popStyleColor()
        }
    }

    inline fun tooltip(block: () -> Unit) {
        ImGui.beginTooltip()
        try {
            block()
        } finally {
            ImGui.endTooltip()
        }
    }

    inline fun withStyleVar(styleVar: Int, value: Float, block: () -> Unit) {
        pushStyleVar(styleVar, value)
        try {
            block()
        } finally {
            popStyleVar()
        }
    }

    inline fun withStyleVar(styleVar: Int, valueX: Float, valueY: Float, block: () -> Unit) {
        pushStyleVar(styleVar, valueX, valueY)
        try {
            block()
        } finally {
            popStyleVar()
        }
    }

    fun helpMarker(description: String) {
        textDisabled("(?)")
        if (ImGui.isItemHovered()) {
            tooltip {
                pushTextWrapPos(getFontSize() * 35f)
                textUnformatted(description)
                popTextWrapPos()
            }
        }
    }

    inline fun calcTextSize(str: String) = calcTextSize(str, ImGui::calcTextSize)

    inline fun calcTextSize(str: String, calc: (ImVec2, String) -> Unit): ImVec2 {
        val dst = ImVec2(0f, 0f)
        calc(dst, str)
        return dst
    }

    inline fun <R> wrapImBool(property: KMutableProperty0<Boolean>, block: (ImBoolean) -> R): R {
        val bool = ImBoolean(property())
        try {
            return block(bool)
        } finally {
            property.set(bool.get())
        }
    }

    inline fun wrapImBool(value: Boolean, block: (ImBoolean) -> Unit) {
        val bool = ImBoolean(value)
        block(bool)
    }

    inline fun wrapImFloat(value: Float, block: (ImFloat) -> Unit) {
        val float = ImFloat(value)
        block(float)
    }

    inline fun <R> wrapImFloat(property: KMutableProperty0<Float>, block: (ImFloat) -> R): R {
        val float = ImFloat(property())
        try {
            return block(float)
        } finally {
            property.set(float.get())
        }
    }

    inline fun <R> wrapImInt(property: KMutableProperty0<Int>, block: (ImInt) -> R): R {
        val int = ImInt(property())
        try {
            return block(int)
        } finally {
            property.set(int.get())
        }
    }

    inline fun wrapSingleIntArray(property: KMutableProperty0<Int>, block: (IntArray) -> Unit) {
        val buf = intArrayOf(property())
        try {
            block(buf)
        } finally {
            property.set(buf[0])
        }
    }

    inline fun wrapSingleFloatArray(property: KMutableProperty0<Float>, block: (FloatArray) -> Unit) {
        val buf = floatArrayOf(property())
        try {
            block(buf)
        } finally {
            property.set(buf[0])
        }
    }

    inline fun <R> wrapImString(property: KMutableProperty0<String>, block: (ImString) -> R): R {
        val buf = ImString(property())
        try {
            return block(buf)
        } finally {
            property.set(buf.get())
        }
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