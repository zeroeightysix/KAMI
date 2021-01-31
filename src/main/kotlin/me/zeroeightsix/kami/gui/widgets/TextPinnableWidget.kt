package me.zeroeightsix.kami.gui.widgets

import imgui.ImGui
import imgui.ImGui.dummy
import imgui.ImGui.sameLine
import imgui.ImGui.separator
import imgui.ImGui.text
import imgui.flag.ImGuiCol
import imgui.flag.ImGuiStyleVar
import imgui.flag.ImGuiWindowFlags
import me.zeroeightsix.kami.gui.ImguiDSL.button
import me.zeroeightsix.kami.gui.ImguiDSL.calcTextSize
import me.zeroeightsix.kami.gui.ImguiDSL.checkbox
import me.zeroeightsix.kami.gui.ImguiDSL.colors
import me.zeroeightsix.kami.gui.ImguiDSL.cursorPosX
import me.zeroeightsix.kami.gui.ImguiDSL.helpMarker
import me.zeroeightsix.kami.gui.ImguiDSL.menu
import me.zeroeightsix.kami.gui.ImguiDSL.menuItem
import me.zeroeightsix.kami.gui.ImguiDSL.window
import me.zeroeightsix.kami.gui.ImguiDSL.windowContentRegionWidth
import me.zeroeightsix.kami.gui.ImguiDSL.withStyleColour
import me.zeroeightsix.kami.gui.ImguiDSL.withStyleVar
import me.zeroeightsix.kami.gui.KamiGuiScreen
import me.zeroeightsix.kami.gui.KamiHud
import me.zeroeightsix.kami.gui.KamiImgui
import me.zeroeightsix.kami.gui.text.CompiledText
import me.zeroeightsix.kami.mc
import me.zeroeightsix.kami.setting.GenerateType
import me.zeroeightsix.kami.sumByFloat
import me.zeroeightsix.kami.util.ResettableLazy
import net.minecraft.util.math.Vec2f

@GenerateType
open class TextPinnableWidget(
    val title: String,
    var text: MutableList<CompiledText> = mutableListOf(CompiledText()),
    position: Position = Position.TOP_LEFT,
    var alignment: Alignment = Alignment.LEFT,
    var ordering: Ordering = Ordering.ORIGINAL
) : PinnableWidget(title, position) {

    private var minecraftFont = true

    private var editWindow = false
    private var editPart: CompiledText.Part? = null

    private var immediateTextDelegate = ResettableLazy {
        val scale = KamiHud.getScale()
        val fontHeight = (mc.textRenderer.fontHeight + 4) * scale

        text.map {
            it.parts.map { part ->
                val str = part.toString()
                val (w, h) = if (part.multiline) {
                    val lines = str.split("\n")
                    (
                        lines.map { slice -> mc.textRenderer.getWidth(slice) }.maxOrNull()
                            ?: 0
                        ) to lines.size * (fontHeight - 2) // multiline strings have less spacing between new lines
                } else {
                    mc.textRenderer.getWidth(str) to fontHeight
                }
                Triple(part, str, Vec2f(w * scale, h))
            }
        }
    }
    private val immediateText by immediateTextDelegate

    override fun fillWindow() {
        val guiOpen = mc.currentScreen is KamiGuiScreen
        // Because of the way minecraft text is rendered, we don't display it when the GUI is open.
        // Otherwise, because it is rendered after imgui, it would always be in the foreground.
        val style = ImGui.getStyle()
        if (minecraftFont && !guiOpen) {
            // For god knows what reason, rendering minecraft text in here results in fucked textures.
            // Even if you revert the GL state to exactly what it was before rendering imgui.
            // So we just toss the text we want to render onto a stack, and we'll draw it after imgui's done.
            val windowPosX = ImGui.getWindowPosX()
            val windowPosY = ImGui.getWindowPosY()
            val windowWidth = ImGui.getWindowWidth()
            KamiImgui.postDraw { matrices ->
                val scale = KamiHud.getScale()
                val x = (windowPosX + style.windowPaddingX) / scale - 2
                var y = (windowPosY + style.windowPaddingY) / scale + 2

                for (triplets in immediateText) {
                    fun calcFullWidth() = triplets.map { it.third.x }.sum()

                    var xOffset = when (alignment) {
                        Alignment.LEFT -> 0f
                        Alignment.CENTER -> ((windowWidth - calcFullWidth()) * 0.5f - style.windowPaddingX) / scale
                        Alignment.RIGHT -> ((windowWidth - calcFullWidth()) - style.windowPaddingY * 2) / scale
                    }

                    for ((part, str, _) in triplets) {
                        if (part.multiline) {
                            val codes = part.codes
                            var lastWidth = 0f
                            str.split("\n").let { lines ->
                                when (ordering) {
                                    Ordering.ORIGINAL -> lines
                                    Ordering.WIDTH_ASCENDING -> lines.sortedBy { mc.textRenderer.getWidth(it) }
                                    Ordering.WIDTH_DESCENDING -> lines.sortedByDescending {
                                        mc.textRenderer.getWidth(
                                            it
                                        )
                                    }
                                }
                            }.forEach {
                                val localXOffset = when (alignment) {
                                    Alignment.LEFT -> xOffset
                                    Alignment.CENTER -> {
                                        ((windowWidth - mc.textRenderer.getWidth(it)) * 0.5f - style.windowPaddingX) / scale
                                    }
                                    Alignment.RIGHT -> {
                                        (windowWidth - style.windowPaddingX * 2) / scale - mc.textRenderer.getWidth(
                                            it
                                        )
                                    }
                                }
                                val width = if (part.shadow)
                                    mc.textRenderer.drawWithShadow(
                                        matrices,
                                        codes + it,
                                        x + localXOffset,
                                        y,
                                        part.currentColourARGB()
                                    ) - (x + localXOffset)
                                else
                                    mc.textRenderer.draw(
                                        matrices,
                                        codes + it,
                                        x + localXOffset,
                                        y,
                                        part.currentColourARGB()
                                    ) - (x + localXOffset)
                                lastWidth = width
                                y += mc.textRenderer.fontHeight + 3
                            }
                            xOffset += lastWidth - 1
                            y -= mc.textRenderer.fontHeight + 3
                            part.resetMultilinePattern()
                        } else {
                            val strCodes = part.codes + str
                            val width = if (part.shadow)
                                mc.textRenderer.drawWithShadow(
                                    matrices,
                                    strCodes,
                                    x + xOffset,
                                    y,
                                    part.currentColourARGB()
                                ) - (x + xOffset)
                            else
                                mc.textRenderer.draw(
                                    matrices,
                                    strCodes,
                                    x + xOffset,
                                    y,
                                    part.currentColourARGB()
                                ) - (x + xOffset)
                            xOffset += width - 1
                        }
                    }
                    y += mc.textRenderer.fontHeight + 5
                }
            }
        } else {
            var empty = guiOpen
            for (triplets in immediateText) {
                var same = false

                fun calcFullWidth() = triplets.map { calcTextSize(it.second).x }.sum()

                fun align(width: Float = calcFullWidth()) = when (alignment) {
                    Alignment.LEFT -> Unit
                    Alignment.CENTER -> cursorPosX = (windowContentRegionWidth - width).coerceAtLeast(0f) * 0.5f
                    Alignment.RIGHT ->
                        cursorPosX =
                            (windowContentRegionWidth - width).coerceAtLeast(0f) + style.windowPaddingX
                }

                for ((part, str, _) in triplets) {
                    val notBlank = str.isNotBlank()
                    if (empty && notBlank) empty =
                        false // We've reached a part that had content: no need to display the 'empty' message

                    if (notBlank) {
                        // Sets the text colour to the current part's colour
                        withStyleColour(ImGuiCol.Text, part.currentColour()) {
                            // If this isn't the first part in the line, make sure it is rendered on the same line
                            if (same) sameLine(0f, 0f)
                            else {
                                // Because we're beginning a new line, we need to also align that line.
                                align()
                                // Mark that the next part has to be on the same line
                                same = true
                            }

                            // We need a different rendering strategy for **aligned** multiline strings.
                            // imgui doesn't support them, so we need to align each line ourselves
                            // imgui CAN handle the 'left' alignment (as it is the only alignment)
                            if (part.multiline && alignment !== Alignment.LEFT) {
                                // As long as we're rendering the multiline string, we don't want any spacing between the lines.
                                withStyleVar(ImGuiStyleVar.ItemSpacing, 0f, 0f) {
                                    // manually align each line
                                    str.split("\n").let { lines ->
                                        when (ordering) {
                                            Ordering.ORIGINAL -> lines
                                            Ordering.WIDTH_ASCENDING -> lines.sortedBy { calcTextSize(it).x }
                                            Ordering.WIDTH_DESCENDING -> lines.sortedByDescending { calcTextSize(it).x }
                                        }
                                    }.forEach {
                                        align(calcTextSize(it).x)
                                        text(it)
                                    }
                                }
                            } else {
                                text(str) // Render the string of this part
                            }
                        }
                    }

                    if (part.multiline) part.resetMultilinePattern()
                }
            }
            if (empty) {
                withStyleColour(ImGuiCol.Text, ImGuiCol.TextDisabled) {
                    text("$title (empty)")
                }
            }
        }
    }

    override fun preWindow() {
        val guiOpen = mc.currentScreen is KamiGuiScreen

        if (guiOpen && editWindow) {
            editWindow()
        }

        if (minecraftFont && !guiOpen && text.isNotEmpty()) {
            val style = ImGui.getStyle()
            val width = (
                immediateText.map {
                    it.sumByFloat { it.third.x }
                }.maxOrNull() ?: 0f
                ) + style.windowPaddingX * 2
            val height = immediateText.mapNotNull {
                it.map { it.third.y }.maxOrNull()
            }.sum() + style.windowPaddingY * 2
            ImGui.setNextWindowSize(width, height)
        }
    }

    override fun postWindow() {
        immediateTextDelegate.invalidate()
    }

    private fun editWindow() {
        window("Edit $title", ::editWindow, ImGuiWindowFlags.AlwaysAutoResize) {
            if (text.isEmpty()) {
                button("New line") {
                    text.add(CompiledText())
                }
            }

            val iterator = text.listIterator()
            var index = 0
            for (compiled in iterator) {
                compiled.edit(
                    "$index",
                    highlightSelected = true,
                    plusButtonExtra = {
                        menu("Line") {
                            menuItem("Before") {
                                iterator.previous()
                                iterator.add(CompiledText())
                                iterator.next()
                            }

                            menuItem("After") {
                                iterator.add(CompiledText())
                            }
                        }
                    }
                )
                // If the selected part for this CompiledText is nonnull
                compiled.selectedPart?.let { selected ->
                    // and not the current editPart,
                    if (selected == editPart) return@let

                    // Reset all other selected parts, and set the editPart to this selected part.
                    text.forEach { if (it != compiled) it.selectedPart = null }
                    this.editPart = selected

                    // This is to maintain only one selected part across all lines.
                }

                val colour = ImGui.getStyle().colors[ImGuiCol.Button]
                withStyleColour(
                    ImGuiCol.Button,
                    colour[0] * 0.7f,
                    colour[1] * 0.7f,
                    colour[2] * 0.7f,
                    colour[3] * 0.7f
                ) {
                    sameLine(0f, 4f)
                    button("-###minus-button-$index") {
                        iterator.remove()
                        editPart = null // In case the editPart was in this line. If it wasn't, we don't really care.
                    }
                }

                index++
            }
            dummy(0f, 0f) // Put a dummy widget here so the next widget isn't on the same line
            editPart?.let {
                separator()

                it.edit(
                    true,
                    if (minecraftFont) CompiledText.Part.FormattingEditMode.ENABLED else CompiledText.Part.FormattingEditMode.DISABLED
                )
            }
        }
    }

    override fun fillStyle() {
        super.fillStyle()
        checkbox("Minecraft font", ::minecraftFont)
        sameLine()
        helpMarker("Only visible when GUI is closed.")

        menu("Alignment") {
            menuItem("Left", "", alignment == Alignment.LEFT) { alignment = Alignment.LEFT }
            menuItem("Center", "", alignment == Alignment.CENTER) { alignment = Alignment.CENTER }
            menuItem("Right", "", alignment == Alignment.RIGHT) { alignment = Alignment.RIGHT }
        }

        menu("Multiline ordering") {
            menuItem("None", "", ordering == Ordering.ORIGINAL) { ordering = Ordering.ORIGINAL }
            menuItem("Ascending", "", ordering == Ordering.WIDTH_ASCENDING) { ordering = Ordering.WIDTH_ASCENDING }
            menuItem("Descending", "", ordering == Ordering.WIDTH_DESCENDING) { ordering = Ordering.WIDTH_DESCENDING }
        }
    }

    override fun fillContextMenu() {
        menuItem("Edit") {
            editWindow = true
        }
    }

    @GenerateType
    enum class Alignment(val x: Float) {
        LEFT(0f), CENTER(0.5f), RIGHT(1f);

        val y = 0.5f
    }

    @GenerateType
    enum class Ordering {
        ORIGINAL, WIDTH_ASCENDING, WIDTH_DESCENDING
    }
}