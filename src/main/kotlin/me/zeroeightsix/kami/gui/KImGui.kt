package me.zeroeightsix.kami.gui

import imgui.ImGui
import imgui.flag.ImGuiCol
import kotlin.reflect.KMutableProperty0
import me.zeroeightsix.kami.gui.ImguiDSL.addFrame
import me.zeroeightsix.kami.gui.ImguiDSL.colour
import me.zeroeightsix.kami.gui.ImguiDSL.cursorPosX
import me.zeroeightsix.kami.gui.ImguiDSL.get
import me.zeroeightsix.kami.gui.ImguiDSL.plus

/**
 * A button for entry of a single character. It will breathe while active to indicate that the button is consuming inputs.
 *
 * @return `true` if `char` was updated
 */
fun charButton(strId: String, char: KMutableProperty0<Char>, pressText: String? = "Press a key") {
    var value by char
    val frameHeight = ImGui.getFrameHeight()
    val framePaddingY = ImGui.getStyle().framePaddingY
    val drawList = ImGui.getWindowDrawList()

    ImguiDSL.withId(strId) {
        val cursorPos = ImguiDSL.cursorPos
        val (x, y) = ImguiDSL.windowPos + cursorPos
        cursorPosX += (frameHeight - ImguiDSL.calcTextSize(value.toString()).x) / 2f
        ImGui.text(value.toString())
        var hovered = ImGui.isItemHovered()
        ImguiDSL.cursorPos = cursorPos

        ImguiDSL.invisibleButton(value.toString(), frameHeight, frameHeight) {}

        hovered = hovered || ImGui.isItemHovered()
        drawList.addFrame(
            x,
            y,
            x + frameHeight,
            y + frameHeight,
            (if (hovered) ImGui.getStyle()[ImGuiCol.FrameBgHovered] else ImGui.getStyle()[ImGuiCol.FrameBg]).colour
        )

        ImGui.sameLine(0f, ImGui.getStyle().itemInnerSpacingX)
        ImguiDSL.cursorPosY += (framePaddingY / 2f)
        ImGui.text(if (hovered) pressText else strId)
        ImguiDSL.cursorPosY -= (framePaddingY / 2f)

        if (hovered) {
            ImGui.captureKeyboardFromApp()

            val c = KamiImgui.charQueue.removeFirstOrNull() ?: return@withId

            value = c.first
        }
    }
}