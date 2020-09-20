package me.zeroeightsix.kami.gui.widgets

import glm_.vec2.Vec2
import imgui.Col
import imgui.ImGui
import imgui.ImGui.calcTextSize
import imgui.ImGui.currentWindow
import imgui.ImGui.cursorPosX
import imgui.ImGui.dummy
import imgui.ImGui.popStyleColor
import imgui.ImGui.pushStyleColor
import imgui.ImGui.sameLine
import imgui.ImGui.separator
import imgui.ImGui.setNextWindowSize
import imgui.ImGui.style
import imgui.ImGui.text
import imgui.WindowFlag
import imgui.api.demoDebugInformations
import imgui.dsl.button
import imgui.dsl.checkbox
import imgui.dsl.menu
import imgui.dsl.menuItem
import imgui.dsl.window
import imgui.dsl.withStyleColor
import imgui.get
import me.zeroeightsix.kami.gui.KamiGuiScreen
import me.zeroeightsix.kami.gui.KamiHud
import me.zeroeightsix.kami.gui.text.CompiledText
import me.zeroeightsix.kami.mc
import me.zeroeightsix.kami.setting.GenerateType
import me.zeroeightsix.kami.sumByFloat
import me.zeroeightsix.kami.tempSet
import me.zeroeightsix.kami.util.ResettableLazy

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
                        lines.map { slice -> mc.textRenderer.getWidth(slice) }.max()
                            ?: 0
                        ) to lines.size * (fontHeight - 2) // multiline strings have less spacing between new lines
                } else {
                    mc.textRenderer.getWidth(str) to fontHeight
                }
                Triple(part, str, Vec2(w * scale, h))
            }
        }
    }
    private val immediateText by immediateTextDelegate

    override fun fillWindow() {
        val guiOpen = mc.currentScreen is KamiGuiScreen
        // Because of the way minecraft text is rendered, we don't display it when the GUI is open.
        // Otherwise, because it is rendered after imgui, it would always be in the foreground.
        if (minecraftFont && !guiOpen) {
            val rect = currentWindow.rect()
            currentWindow.drawList.addCallback({ _, cmd ->
                // For god knows what reason, rendering minecraft text in here results in fucked textures.
                // Even if you revert the GL state to exactly what it was before rendering imgui.
                // So we just toss the text we want to render onto a stack, and we'll draw it after imgui's done.
                KamiHud.postDraw { matrices ->
                    val scale = KamiHud.getScale()
                    val x = (cmd.clipRect.x + style.windowPadding.x) / scale - 2
                    var y = (cmd.clipRect.y + style.windowPadding.y) / scale + 2

                    for (triplets in immediateText) {
                        fun calcFullWidth() = triplets.map { it.third.x }.sum()

                        var xOffset = when (alignment) {
                            Alignment.LEFT -> 0f
                            Alignment.CENTER -> ((rect.width - calcFullWidth()) * 0.5f - style.windowPadding.x) / scale
                            Alignment.RIGHT -> ((rect.width - calcFullWidth()) - style.windowPadding.x * 2) / scale
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
                                            ((rect.width - mc.textRenderer.getWidth(it)) * 0.5f - style.windowPadding.x) / scale
                                        }
                                        Alignment.RIGHT -> {
                                            (rect.width - style.windowPadding.x * 2) / scale - mc.textRenderer.getWidth(
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
            })
        } else {
            var empty = guiOpen
            for (triplets in immediateText) {
                var same = false

                fun calcFullWidth() = triplets.map { calcTextSize(it.second).x }.sum()

                fun align(width: Float = calcFullWidth()) = when (alignment) {
                    Alignment.LEFT -> Unit
                    Alignment.CENTER -> cursorPosX = (currentWindow.innerRect.width - width).coerceAtLeast(0f) * 0.5f
                    Alignment.RIGHT ->
                        cursorPosX =
                            (currentWindow.workRect.width - width).coerceAtLeast(0f) + style.windowPadding.x
                }

                for ((part, str, _) in triplets) {
                    val notBlank = str.isNotBlank()
                    if (empty && notBlank) empty =
                        false // We've reached a part that had content: no need to display the 'empty' message

                    if (notBlank) {
                        // Sets the text colour to the current part's colour
                        pushStyleColor(Col.Text, part.currentColour())

                        // If this isn't the first part in the line, make sure it is rendered on the same line
                        if (same) sameLine(spacing = 0f)
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
                            style.itemSpacing::y.tempSet(0f) {
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

                        popStyleColor() // Remove the text colour (styles are stacked, instead of state-based!)
                    }

                    if (part.multiline) part.resetMultilinePattern()
                }
            }
            if (empty) {
                pushStyleColor(Col.Text, ImGui.style.colors[Col.TextDisabled])
                text("$title (empty)")
                popStyleColor()
            }
        }
    }

    override fun preWindow() {
        val guiOpen = mc.currentScreen is KamiGuiScreen

        if (guiOpen && editWindow) {
            editWindow()
        }

        if (minecraftFont && !guiOpen && text.isNotEmpty()) {
            val width = (
                immediateText.map {
                    it.sumByFloat { it.third.x }
                }.max() ?: 0f
                ) + style.windowPadding.x * 2
            val height = immediateText.mapNotNull {
                it.map { it.third.y }.max()
            }.sum() + style.windowPadding.y * 2
            setNextWindowSize(Vec2(width, height))
        }
    }

    override fun postWindow() {
        immediateTextDelegate.invalidate()
    }

    private fun editWindow() {
        window("Edit $title", ::editWindow, WindowFlag.AlwaysAutoResize.i) {
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
                compiled.selectedPart?.let {
                    // and not the current editPart,
                    if (it == editPart) return@let

                    // Reset all other selected parts, and set the editPart to this selected part.
                    text.forEach { if (it != compiled) it.selectedPart = null }
                    this.editPart = it

                    // This is to maintain only one selected part across all lines.
                }

                withStyleColor(Col.Button, ImGui.style.colors[Col.Button.i] * 0.7f) {
                    sameLine(spacing = 4f)
                    button("-###minus-button-$index") {
                        iterator.remove()
                        editPart = null // In case the editPart was in this line. If it wasn't, we don't really care.
                    }
                }

                index++
            }
            dummy(Vec2(0, 0)) // Put a dummy widget here so the next widget isn't on the same line
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
        checkbox("Minecraft font", ::minecraftFont) {}
        sameLine()
        demoDebugInformations.helpMarker("Only visible when GUI is closed.")

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
    enum class Alignment(val f: Float) {
        LEFT(0f), CENTER(0.5f), RIGHT(1f);
        
        val vecAlignment = Vec2(this.f, 0.5f)
    }

    @GenerateType
    enum class Ordering {
        ORIGINAL, WIDTH_ASCENDING, WIDTH_DESCENDING
    }
}
