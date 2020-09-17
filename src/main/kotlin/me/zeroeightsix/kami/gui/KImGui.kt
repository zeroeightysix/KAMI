package me.zeroeightsix.kami.gui

import glm_.vec2.Vec2
import imgui.*
import imgui.ImGui.buttonBehavior
import imgui.ImGui.calcTextSize
import imgui.ImGui.clearActiveID
import imgui.ImGui.currentWindow
import imgui.ImGui.frameHeight
import imgui.ImGui.io
import imgui.ImGui.itemAdd
import imgui.ImGui.itemSize
import imgui.ImGui.markItemEdited
import imgui.ImGui.renderNavHighlight
import imgui.ImGui.renderText
import imgui.ImGui.setActiveID
import imgui.ImGui.style
import imgui.api.g
import imgui.internal.classes.Rect
import imgui.internal.sections.ItemFlags
import java.util.*
import kotlin.reflect.KMutableProperty0

// This file is not called 'kotlin imgui', but rather KAMI imgui.
// It contains useful wrappers, or code that extends the imgui library.

private val map: WeakHashMap<KMutableProperty0<String>, ByteArray> = WeakHashMap()

private val strResizeCallback: InputTextCallback = { data ->
    if (data.eventFlag == InputTextFlag.CallbackResize.i) {
        @Suppress("UNCHECKED_CAST")
        val property = data.userData as KMutableProperty0<String>
        val str = data.buf.cStr
        property(str)
        data.buf = str.toByteArray(data.bufSize + 1)
        map[property] = data.buf
        true
    } else false
}

/**
 * [ImGui.inputText] wired to work with a static reference to [String]
 */
fun inputText(label: String, text: KMutableProperty0<String>, flags: ItemFlags = 0): Boolean {
    assert(flags hasnt InputTextFlag.CallbackResize)
    return ImGui.inputText(
        label,
        map.computeIfAbsent(text) { it().let { it.toByteArray(it.toByteArray().size + 1) } },
        flags or InputTextFlag.CallbackResize,
        strResizeCallback,
        text
    ).also { updated ->
        if (updated)
            map[text]?.cStr?.let { text(it) }
    }
}

/**
 * A button for entry of a single character. It will breathe while active to indicate that the button is consuming inputs.
 *
 * @return `true` if `char` was updated
 */
fun charButton(strId: String, char: KMutableProperty0<Char>, pressText: String? = "Press a key"): Boolean {
    var v by char
    val window = currentWindow
    if (window.skipItems) return false

    val id = window.getID(strId)
    val isActive = g.activeId == id
    val label = if (isActive && pressText != null) {
        pressText
    } else strId
    val labelSize = calcTextSize(label, hideTextAfterDoubleHash = true)

    val squareSz = frameHeight
    val pos = Vec2(window.dc.cursorPos)
    val totalBb = Rect(
        pos,
        pos + Vec2(
            squareSz + if (labelSize.x > 0f) style.itemInnerSpacing.x + labelSize.x else 0f,
            labelSize.y + style.framePadding.y * 2f
        )
    )
    itemSize(totalBb, style.framePadding.y)

    if (!itemAdd(totalBb, id))
        return false

    val (pressed, hovered, held) = buttonBehavior(totalBb, id)
    if (pressed) {
        setActiveID(id, window)
    }

    val keyBb = Rect(pos, pos + squareSz)
    renderNavHighlight(totalBb, id)
    val col = if (held && hovered) Col.FrameBgActive else if (hovered) Col.FrameBgHovered else Col.FrameBg
    ImGui.renderFrame(keyBb.min, keyBb.max, col.u32, true, style.frameRounding)

    if (labelSize.x > 0f)
        renderText(Vec2(keyBb.max.x + style.itemInnerSpacing.x, keyBb.min.y + style.framePadding.y), label)

    val key = v.toString()
    val keySize = calcTextSize(key)
    renderText(Vec2(keyBb.min.x + (squareSz - keySize.x) * 0.5f, keyBb.min.y + style.framePadding.y), key)

    if (isActive && io.inputQueueCharacters.isNotEmpty()) {
        // Remove the first character from the queue, which we consume.
        val c = io.inputQueueCharacters.removeAt(0)
        v = c
        markItemEdited(id)

        // No longer active after consuming input
        clearActiveID()
        return true
    }

    return false
}
