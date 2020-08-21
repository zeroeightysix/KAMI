package me.zeroeightsix.kami.gui.widgets

import glm_.vec2.Vec2
import glm_.vec4.Vec4
import imgui.*
import imgui.ImGui.acceptDragDropPayload
import imgui.ImGui.calcTextSize
import imgui.ImGui.colorEditVec4
import imgui.ImGui.currentWindow
import imgui.ImGui.cursorPosX
import imgui.ImGui.dragInt
import imgui.ImGui.dummy
import imgui.ImGui.getMouseDragDelta
import imgui.ImGui.inputText
import imgui.ImGui.isItemActive
import imgui.ImGui.isItemHovered
import imgui.ImGui.openPopup
import imgui.ImGui.popStyleColor
import imgui.ImGui.pushStyleColor
import imgui.ImGui.resetMouseDragDelta
import imgui.ImGui.sameLine
import imgui.ImGui.separator
import imgui.ImGui.setNextWindowSize
import imgui.ImGui.style
import imgui.ImGui.text
import imgui.ImGui.textDisabled
import imgui.api.demoDebugInformations
import imgui.dsl.button
import imgui.dsl.checkbox
import imgui.dsl.combo
import imgui.dsl.dragDropTarget
import imgui.dsl.menu
import imgui.dsl.menuItem
import imgui.dsl.popupContextItem
import imgui.dsl.window
import me.zeroeightsix.kami.*
import me.zeroeightsix.kami.gui.KamiGuiScreen
import me.zeroeightsix.kami.gui.KamiHud
import me.zeroeightsix.kami.util.LagCompensator
import me.zeroeightsix.kami.util.ResettableLazy
import net.minecraft.util.math.MathHelper
import kotlin.collections.map
import kotlin.math.abs
import kotlin.math.floor

open class TextPinnableWidget(
    val title: String,
    var text: MutableList<CompiledText> = mutableListOf(CompiledText()),
    position: Position = Position.TOP_LEFT
) : PinnableWidget(title, position) {

    private var minecraftFont = true

    private var editWindow = false
    private var editPart: CompiledText.Part? = null
    private var editColourComboIndex = 0

    private var textAlignment = Alignment.LEFT

    private var immediateTextDelegate = ResettableLazy {
        val scale = KamiHud.getScale()
        val fontHeight = (mc.textRenderer.fontHeight + 4) * scale

        text.map {
            it.parts.map {
                val str = it.toString()
                val (w, h) = if (it.multiline) {
                    val lines = str.split("\n")
                    (lines.map { slice -> mc.textRenderer.getWidth(slice) }.max() ?: 0) to lines.size * fontHeight
                } else {
                    mc.textRenderer.getWidth(str) to fontHeight
                }
                Triple(it, str, Vec2(w * scale, h))
            }
        }
    }
    private val immediateText by immediateTextDelegate

    companion object {
        private infix fun String.const(strProvider: () -> String) =
            this to { CompiledText.ConstantVariable(this, string = strProvider()) }

        private infix fun String.numeric(valProvider: () -> Double) =
            this to { CompiledText.NumericalVariable(this, valProvider, 0) }

        val varMap: MutableMap<String, () -> CompiledText.Variable> = mutableMapOf(
            "none" const { "No variable selected " },
            "x" numeric { mc.player?.pos?.x ?: 0.0 },
            "y" numeric { mc.player?.pos?.y ?: 0.0 },
            "z" numeric { mc.player?.pos?.z ?: 0.0 },
            "yaw" numeric { MathHelper.wrapDegrees(mc.player?.yaw?.toDouble() ?: 0.0) },
            "pitch" numeric { mc.player?.pitch?.toDouble() ?: 0.0 },
            "tps" numeric { LagCompensator.tickRate.toDouble() },
            "username" const { mc.session.username },
            "version" const { KamiMod.MODVER },
            "client" const { KamiMod.MODNAME },
            "kanji" const { KamiMod.KAMI_KANJI },
            "modules" to { modulesVariable }
        )
    }

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

                        var xOffset = when (textAlignment) {
                            Alignment.LEFT -> 0f
                            Alignment.CENTER -> ((rect.width - calcFullWidth()) * 0.5f - style.windowPadding.x) / scale
                            Alignment.RIGHT -> ((rect.width - calcFullWidth()) - style.windowPadding.x * 2) / scale
                        }

                        for ((command, str, dim) in triplets) {
                            if (command.multiline) {
                                val codes = command.codes
                                var lastWidth = 0f
                                str.split("\n").forEach {
                                    val localXOffset = when (textAlignment) {
                                        Alignment.LEFT -> xOffset
                                        Alignment.CENTER -> {
                                            (rect.width - mc.textRenderer.getWidth(it)) * 0.5f
                                        }
                                        Alignment.RIGHT -> {
                                            rect.width - mc.textRenderer.getWidth(it)
                                        }
                                    }
                                    val width = if (command.shadow)
                                        mc.textRenderer.drawWithShadow(
                                            matrices,
                                            codes + it,
                                            x + localXOffset,
                                            y,
                                            command.currentColourARGB()
                                        ) - (x + localXOffset)
                                    else
                                        mc.textRenderer.draw(
                                            matrices,
                                            codes + it,
                                            x + localXOffset,
                                            y,
                                            command.currentColourARGB()
                                        ) - (x + localXOffset)
                                    lastWidth = width
                                    y += mc.textRenderer.fontHeight + 3
                                }
                                xOffset += lastWidth - 1
                                y -= mc.textRenderer.fontHeight + 3
                                command.resetMultilinePattern()
                            } else {
                                val strCodes = command.codes + str
                                val width = if (command.shadow)
                                    mc.textRenderer.drawWithShadow(
                                        matrices,
                                        strCodes,
                                        x + xOffset,
                                        y,
                                        command.currentColourARGB()
                                    ) - (x + xOffset)
                                else
                                    mc.textRenderer.draw(
                                        matrices,
                                        strCodes,
                                        x + xOffset,
                                        y,
                                        command.currentColourARGB()
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

                if (textAlignment === Alignment.CENTER) {
                    cursorPosX = (currentWindow.innerRect.width - calcFullWidth()).coerceAtLeast(0f) * 0.5f
                } else if (textAlignment === Alignment.RIGHT) {
                    cursorPosX =
                        (currentWindow.workRect.width - calcFullWidth()).coerceAtLeast(0f) + style.windowPadding.x
                }

                for ((part, str, _) in triplets) {
                    pushStyleColor(Col.Text, part.currentColour())
                    if (same) sameLine(spacing = 0f)
                    else same = true
                    val notBlank = str.isNotBlank()
                    if (empty && notBlank) empty = false
                    if (notBlank) {
                        text(str)
                    }
                    popStyleColor()

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
            val width = (immediateText.map {
                it.sumByFloat { it.third.x }
            }.max() ?: 0f) + style.windowPadding.x * 2
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
            fun setEditPart(part: CompiledText.Part) {
                editPart = part
                editColourComboIndex = CompiledText.Part.ColourMode.values().indexOf(part.colourMode)
            }

            if (text.isEmpty()) {
                button("New line") {
                    text.add(CompiledText())
                }
            }

            val iterator = text.listIterator()
            var index = 0
            for (compiled in iterator) {
                val parts = compiled.parts
                with (parts.listIterator()) {
                    forEachRemainingIndexed { n, part ->
                        val highlight = editPart == part
                        if (highlight) {
                            pushStyleColor(Col.Text, (style.colors[Col.Text.i] / 1.2f))
                        }
                        button("${part.editLabel}###part-button-${part.hashCode()}") {
                            setEditPart(part)
                        }
                        popupContextItem {
                            menuItem("Remove") {
                                remove()
                            }

                        }

                        if (isItemActive && !isItemHovered()) {
                            val nNext = n + if (getMouseDragDelta(MouseButton.Left).x < 0f) -1 else 1
                            if (nNext in parts.indices) {
                                parts[n] = parts[nNext]
                                parts[nNext] = part
                                resetMouseDragDelta()
                            }
                        }

                        dragDropTarget {
                            acceptDragDropPayload(PAYLOAD_TYPE_COLOR_4F)?.let {
                                part.colour = it.data!! as Vec4
                            }
                        }
                        sameLine() // The next button should be on the same line
                        if (highlight) {
                            popStyleColor()
                        }
                    }
                }
                pushStyleColor(Col.Button, style.colors[Col.Button.i] * 0.7f)
                button("+###plus-button-$index") {
                    openPopup("plus-popup-$index")
                }
                popupContextItem("plus-popup-$index") {
                    fun addPart(part: CompiledText.Part) {
                        val mutable = compiled.parts.toMutableList()
                        mutable.add(part)
                        compiled.parts = mutable
                        setEditPart(part)
                    }
                    menuItem("Text") { addPart(CompiledText.LiteralPart("Text")) }
                    menuItem("Variable") { addPart(CompiledText.VariablePart(varMap["none"]!!())) }
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

                sameLine(spacing = 4f)
                button("-###minus-button-$index") {
                    iterator.remove()
                    editPart = null // In case the editPart was in this line. If it wasn't, we don't really care.
                }
                popStyleColor()

                index++
            }
            dummy(Vec2(0, 0)) // Put a dummy widget here so the next widget isn't on the same line
            editPart?.let {
                separator()
                val col = it.colour
                combo("Colour mode", ::editColourComboIndex, it.multiline.to(CompiledText.Part.ColourMode.listMultiline, CompiledText.Part.ColourMode.listNormal) ) {
                    it.colourMode = CompiledText.Part.ColourMode.values()[editColourComboIndex]
                }

                when (it.colourMode) {
                    CompiledText.Part.ColourMode.STATIC -> {
                        if (colorEditVec4("Colour", col, flags = ColorEditFlag.AlphaBar.i)) {
                            it.colour = col
                        }
                    }
                    CompiledText.Part.ColourMode.ALTERNATING -> {
                        it.colours.forEachIndexed { i, vec ->
                            colorEditVec4("Colour $i", vec, flags = ColorEditFlag.AlphaBar.i)
                        }

                        // TODO: Allow colours to be added / removed
                    }
                    else -> {}
                }

                it.edit(varMap)

                if (minecraftFont) {
                    val shadow = booleanArrayOf(it.shadow)
                    val bold = booleanArrayOf(it.bold)
                    val italic = booleanArrayOf(it.italic)
                    val underline = booleanArrayOf(it.underline)
                    val strikethrough = booleanArrayOf(it.strike)
                    val obfuscated = booleanArrayOf(it.obfuscated)
                    if (ImGui.checkbox("Shadow", shadow)) it.shadow = !it.shadow
                    sameLine()
                    if (ImGui.checkbox("Bold", bold)) it.bold = !it.bold
                    sameLine()
                    if (ImGui.checkbox("Italic", italic)) it.italic = !it.italic
                    // newline
                    if (ImGui.checkbox("Underline", underline)) it.underline = !it.underline
                    sameLine()
                    if (ImGui.checkbox("Cross out", strikethrough)) it.strike = !it.strike
                    sameLine()
                    if (ImGui.checkbox("Obfuscated", obfuscated)) it.obfuscated = !it.obfuscated
                } else {
                    textDisabled("Styles disabled")
                    if (ImGui.isItemHovered()) {
                        ImGui.beginTooltip()
                        ImGui.pushTextWrapPos(ImGui.fontSize * 35f)
                        ImGui.textEx("Enable minecraft font rendering to enable styles")
                        ImGui.popTextWrapPos()
                        ImGui.endTooltip()
                    }
                    sameLine()
                    button("Enable") {
                        minecraftFont = true
                    }
                }
            }
        }
    }

    override fun fillStyle() {
        super.fillStyle()
        checkbox("Minecraft font", ::minecraftFont) {}
        sameLine()
        demoDebugInformations.helpMarker("Only visible when GUI is closed.")

        menu("Alignment") {
            menuItem("Left", "", textAlignment == Alignment.LEFT) { textAlignment = Alignment.LEFT }
            menuItem("Center", "", textAlignment == Alignment.CENTER) { textAlignment = Alignment.CENTER }
            menuItem("Right", "", textAlignment == Alignment.RIGHT) { textAlignment = Alignment.RIGHT }
        }
    }

    override fun fillContextMenu() {
        menuItem("Edit") {
            editWindow = true
        }
    }

    class CompiledText(
        var parts: MutableList<Part> = mutableListOf()
    ) {

        override fun toString(): String {
            var buf = ""
            for (part in parts) buf += part.toString()
            return buf
        }

        abstract class Part(
            var obfuscated: Boolean = false,
            // _names because they have mirrors with custom setters in the class
            _bold: Boolean = false,
            _strike: Boolean = false,
            _underline: Boolean = false,
            _italic: Boolean = false,
            var shadow: Boolean = true,
            var colourMode: ColourMode = ColourMode.STATIC,
            var extraspace: Boolean = true
        ) {
            private fun toCodes(): String {
                return (if (obfuscated) "§k" else "") +
                        (if (bold) "§l" else "") +
                        (if (strike) "§m" else "") +
                        (if (underline) "§n" else "") +
                        (if (italic) "§o" else "")
            }

            open val editLabel: String
                get() = toString()
            abstract val multiline: Boolean
            var codes: String = toCodes()

            var bold: Boolean = _bold
                set(value) {
                    field = value
                    codes = toCodes()
                }
            var strike: Boolean = _strike
                set(value) {
                    field = value
                    codes = toCodes()
                }
            var underline: Boolean = _underline
                set(value) {
                    field = value
                    codes = toCodes()
                }
            var italic: Boolean = _italic
                set(value) {
                    field = value
                    codes = toCodes()
                }

            private fun Vec4.toARGB(): Int {
                val r = (x * 255.0F).toInt()
                val g = (y * 255.0F).toInt()
                val b = (z * 255.0F).toInt()
                val a = (w * 255.0F).toInt()
                return (a shl 24) or (r shl 16) or (g shl 8) or b
            }

            // Static colour
            var colour: Vec4 = Vec4(1.0f, 1.0f, 1.0f, 1.0f)
                set(value) {
                    field = value
                    argb = colour.toARGB()
                }
            private var argb: Int = this.colour.toARGB()

            // Alternating colour
            var colours = mutableListOf(
                Vec4(1.0f, 1.0f, 1.0f, 1.0f),
                Vec4(0.5f, 0.5f, 0.5f, 1.0f)
            )
            val coloursIterator = colours.cyclingIterator()

            override fun toString(): String {
                return if (extraspace) " " else ""
            }

            fun currentColour(): Vec4 {
                return when (colourMode) {
                    ColourMode.STATIC -> colour
                    ColourMode.RAINBOW -> KamiMod.rainbow.vec4
                    ColourMode.ALTERNATING -> coloursIterator.next()
                }
            }

            fun currentColourARGB(): Int {
                return currentColour().toARGB()
            }

            /**
             * For parts that might have a coloured pattern; this method must be called when you're done drawing that part.
             */
            fun resetMultilinePattern() {
                coloursIterator.reset()
            }

            enum class ColourMode(private val multilineExclusive: Boolean = false) {
                STATIC, RAINBOW, ALTERNATING(true);

                companion object {
                    val listNormal = values().filter { !it.multilineExclusive }.joinToString("$NUL") { it.name.toLowerCase().capitalize() }
                    val listMultiline = values().joinToString("$NUL") { it.name.toLowerCase().capitalize() }
                }
            }

            abstract fun edit(variableMap: Map<String, () -> Variable>)
        }

        class LiteralPart(
            var string: String,
            obfuscated: Boolean = false,
            bold: Boolean = false,
            strike: Boolean = false,
            underline: Boolean = false,
            italic: Boolean = false,
            shadow: Boolean = true,
            colourMode: ColourMode = ColourMode.STATIC,
            extraspace: Boolean = true
        ) : Part(obfuscated, bold, strike, underline, italic, shadow, colourMode, extraspace) {
            override val multiline = false

            override fun toString(): String {
                return string + super.toString()
            }

            override fun edit(variableMap: Map<String, () -> Variable>) {
                val buf = string.toByteArray(ByteArray((((floor((abs((string.length - 4) / 256) + 1).toDouble()))) * 256).toInt()))
                if (inputText("Text", buf)) {
                    string = buf.backToString()
                }
                sameLine()
                text("+")
                sameLine()
                val space = booleanArrayOf(extraspace)
                if (ImGui.checkbox("Space", space)) {
                    extraspace = space[0]
                }
            }
        }

        class VariablePart(
            var variable: Variable,
            obfuscated: Boolean = false,
            bold: Boolean = false,
            strike: Boolean = false,
            underline: Boolean = false,
            italic: Boolean = false,
            shadow: Boolean = true,
            colourMode: ColourMode = ColourMode.STATIC,
            extraspace: Boolean = true
        ) : Part(obfuscated, bold, strike, underline, italic, shadow, colourMode, extraspace) {
            private var editVarComboIndex = -1
            private var editDigits = (variable is NumericalVariable).then(
                { (variable as NumericalVariable).digits }, { 0 }
            )

            override val multiline: Boolean
                get() = variable.multiline

            override val editLabel: String
                get() = variable.editLabel + super.toString()

            override fun toString(): String {
                return variable.provide() + super.toString()
            }

            override fun edit(variableMap: Map<String, () -> Variable>) {
                if (editVarComboIndex == -1) editVarComboIndex = variableMap.keys.indexOf(this.variable.name)
                combo("Variable", ::editVarComboIndex, variableMap.keys.joinToString(0.toChar().toString())) {
                    val selected: String = variableMap.keys.toList()[editVarComboIndex]
                    val v = (variableMap[selected] ?: error("Invalid item selected")).invoke()
                    if (v is NumericalVariable) {
                        v.digits = editDigits
                    }
                    this.variable = v
                }
                sameLine()
                text("+")
                sameLine()
                val space = booleanArrayOf(extraspace)
                if (ImGui.checkbox("Space", space)) {
                    extraspace = space[0]
                }
                when (val variable = variable) {
                    is NumericalVariable -> {
                        if (dragInt("Digits", ::editDigits, vSpeed = 0.1f, vMin = 0, vMax = 8)) {
                            variable.digits = editDigits
                        }
                    }
                }
                variable.edit(variableMap)
            }
        }

        abstract class Variable(val name: String) {
            abstract val multiline: Boolean
            open val editLabel: String
                get() = provide()

            abstract fun provide(): String
            open fun edit(variableMap: Map<String, () -> Variable>) {}
        }

        class ConstantVariable(name: String, _multiline: Boolean = false, private val string: String) : Variable(name) {
            override val multiline = _multiline

            override fun provide(): String {
                return string
            }
        }

        class NumericalVariable(name: String, private val provider: () -> Double?, var digits: Int = 0) :
            Variable(name) {
            override val multiline = false

            override fun provide(): String {
                val number = provider()
                return String.format("%.${digits}f", number)
            }
        }

        open class StringVariable(name: String, _multiline: Boolean = false, private val provider: () -> String) :
            Variable(name) {
            override val multiline = _multiline

            override fun provide(): String {
                return provider()
            }
        }
    }

    enum class Alignment {
        LEFT, CENTER, RIGHT
    }

}
