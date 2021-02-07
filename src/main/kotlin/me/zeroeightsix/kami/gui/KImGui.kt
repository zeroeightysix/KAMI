package me.zeroeightsix.kami.gui

import imgui.ImGui
import imgui.flag.ImGuiCol
import kotlin.reflect.KMutableProperty0
import me.zeroeightsix.kami.gui.ImguiDSL.addFrame
import me.zeroeightsix.kami.gui.ImguiDSL.calcTextSize
import me.zeroeightsix.kami.gui.ImguiDSL.colour
import me.zeroeightsix.kami.gui.ImguiDSL.cursorPosX
import me.zeroeightsix.kami.gui.ImguiDSL.get
import me.zeroeightsix.kami.gui.ImguiDSL.plus
import me.zeroeightsix.kami.gui.windows.Settings
import me.zeroeightsix.kami.util.Bind

private fun <T, R> inputButton(text: String, value: T, hoverText: String?, width: Float? = null, onHover: () -> R): R? {
    val frameHeight = ImGui.getFrameHeight()
    val framePaddingY = ImGui.getStyle().framePaddingY
    val drawList = ImGui.getWindowDrawList()
    @Suppress("NAME_SHADOWING") val width = width ?: frameHeight

    val cursorPos = ImguiDSL.cursorPos
    val (x, y) = ImguiDSL.windowPos + cursorPos
    cursorPosX += (width - calcTextSize(value.toString()).x) / 2f
    ImGui.text(value.toString())
    var hovered = ImGui.isItemHovered()
    ImguiDSL.cursorPos = cursorPos

    ImguiDSL.invisibleButton(value.toString(), width, frameHeight) {}

    hovered = hovered || ImGui.isItemHovered()
    drawList.addFrame(
        x,
        y,
        x + width,
        y + frameHeight,
        (if (hovered) ImGui.getStyle()[ImGuiCol.FrameBgHovered] else ImGui.getStyle()[ImGuiCol.FrameBg]).colour
    )

    ImGui.sameLine(0f, ImGui.getStyle().itemInnerSpacingX)
    ImguiDSL.cursorPosY += (framePaddingY / 2f)
    ImGui.text(if (hovered) hoverText else text)
    ImguiDSL.cursorPosY -= (framePaddingY / 2f)

    return if (hovered) onHover() else null
}

fun charButton(text: String, char: KMutableProperty0<Char>, pressText: String? = "Type a character") {
    inputButton(text, char.get(), pressText) {
        ImGui.captureKeyboardFromApp()

        char.set((KamiImgui.charQueue.removeFirstOrNull() ?: return@inputButton).first)
    }
}

fun bindButton(text: String, bind: Bind, pressText: String? = "Press a key"): Bind? {
    val width = calcTextSize(bind.toString()).x.coerceAtLeast(20f)
    return inputButton(text, bind, pressText, width + ImGui.getStyle().framePaddingX * 2f) {
        ImGui.captureKeyboardFromApp()
        val c = KamiImgui.keyQueue.removeFirstOrNull() ?: return@inputButton null
        val modifiers = Settings.modifiersEnabled
        return@inputButton Bind(
            ImGui.getIO().keyCtrl && modifiers,
            ImGui.getIO().keyAlt && modifiers,
            ImGui.getIO().keyShift && modifiers,
            c
        )
    }
}