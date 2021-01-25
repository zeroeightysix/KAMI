package me.zeroeightsix.kami.gui

import imgui.ImGui
import kotlin.reflect.KMutableProperty0

/**
 * A button for entry of a single character. It will breathe while active to indicate that the button is consuming inputs.
 *
 * @return `true` if `char` was updated
 */
fun charButton(strId: String, char: KMutableProperty0<Char>, pressText: String? = "Press a key"): Boolean {
//    var v by char
//    val window = currentWindow
//    if (window.skipItems) return false
//
//    val id = window.getID(strId)
//    val isActive = g.activeId == id
//    val label = if (isActive && pressText != null) {
//        pressText
//    } else strId
//    val labelSize = calcTextSize(label, hideTextAfterDoubleHash = true)
//
//    val squareSz = frameHeight
//    val pos = Vec2(window.dc.cursorPos)
//    val totalBb = Rect(
//        pos,
//        pos + Vec2(
//            squareSz + if (labelSize.x > 0f) style.itemInnerSpacing.x + labelSize.x else 0f,
//            labelSize.y + style.framePadding.y * 2f
//        )
//    )
//    itemSize(totalBb, style.framePadding.y)
//
//    if (!itemAdd(totalBb, id))
//        return false
//
//    val (pressed, hovered, held) = buttonBehavior(totalBb, id)
//    if (pressed) {
//        setActiveID(id, window)
//    }
//
//    val keyBb = Rect(pos, pos + squareSz)
//    renderNavHighlight(totalBb, id)
//    val col = if (held && hovered) Col.FrameBgActive else if (hovered) Col.FrameBgHovered else Col.FrameBg
//    ImGui.renderFrame(keyBb.min, keyBb.max, col.u32, true, style.frameRounding)
//
//    if (labelSize.x > 0f)
//        renderText(Vec2(keyBb.max.x + style.itemInnerSpacing.x, keyBb.min.y + style.framePadding.y), label)
//
//    val key = v.toString()
//    val keySize = calcTextSize(key)
//    renderText(Vec2(keyBb.min.x + (squareSz - keySize.x) * 0.5f, keyBb.min.y + style.framePadding.y), key)
//
//    if (isActive && io.inputQueueCharacters.isNotEmpty()) {
//        // Remove the first character from the queue, which we consume.
//        val c = io.inputQueueCharacters.removeAt(0)
//        v = c
//        markItemEdited(id)
//
//        // No longer active after consuming input
//        clearActiveID()
//        return true
//    }
    TODO() // not so easy - looks like the bindings don't quite expose the internals?
}
