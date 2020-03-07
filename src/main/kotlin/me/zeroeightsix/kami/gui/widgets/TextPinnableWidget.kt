package me.zeroeightsix.kami.gui.widgets

import glm_.vec2.Vec2
import glm_.vec4.Vec4
import imgui.*
import imgui.ImGui.acceptDragDropPayload
import imgui.ImGui.colorEditVec4
import imgui.ImGui.currentWindow
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
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.gui.KamiGuiScreen
import me.zeroeightsix.kami.gui.KamiHud
import me.zeroeightsix.kami.util.LagCompensator
import me.zeroeightsix.kami.util.Wrapper
import kotlin.reflect.KMutableProperty0

open class TextPinnableWidget(
    private val title: String,
    private val variableMap: Map<String, () -> CompiledText.Variable> = extendStd(mapOf()),
    private var text: MutableList<CompiledText> = mutableListOf(CompiledText())
) : PinnableWidget(title) {

    private var minecraftFont = true

    private var editWindow = false
    private var editPart: CompiledText.Part? = null
    private var editColourComboIndex = 0

    companion object {
        private var sVarMap: Map<String, () -> CompiledText.Variable>? = null

        internal fun extendStd(extra: Map<String, () -> CompiledText.Variable>): Map<String, () -> CompiledText.Variable> {
            val std: MutableMap<String, () -> CompiledText.Variable> = mutableMapOf(
                Pair("none", { CompiledText.ConstantVariable(string = "No variable selected") }),
                Pair("x", { CompiledText.NumericalVariable({ Wrapper.getPlayer().pos.x }, 0) }),
                Pair("y", { CompiledText.NumericalVariable({ Wrapper.getPlayer().pos.y }, 0) }),
                Pair("z", { CompiledText.NumericalVariable({ Wrapper.getPlayer().pos.z }, 0) }),
                Pair("yaw", { CompiledText.NumericalVariable({ Wrapper.getPlayer().yaw.toDouble() }, 0) }),
                Pair("pitch", { CompiledText.NumericalVariable({ Wrapper.getPlayer().pitch.toDouble() }, 0) }),
                Pair("tps", { CompiledText.NumericalVariable({ LagCompensator.INSTANCE.tickRate.toDouble() }, 0) }),
                Pair("username", { CompiledText.ConstantVariable(string = Wrapper.getMinecraft().session.username) })
            )
            std.putAll(extra)
            sVarMap = std
            return std
        }

        internal fun getVariable(variable: String): CompiledText.Variable {
            val f: () -> CompiledText.Variable = sVarMap!![variable] ?: error("Invalid item selected")
            return f()
        }
    }

    override fun fillWindow(open: KMutableProperty0<Boolean>) {

        val guiOpen = Wrapper.getMinecraft().currentScreen is KamiGuiScreen
        // Because of the way minecraft text is rendered, we don't display it when the GUI is open.
        // Otherwise, because it is rendered after imgui, it would always be in the foreground.
        if (minecraftFont && !guiOpen) {
            currentWindow.drawList.addCallback({ _, cmd ->
                // For god knows what reason, rendering minecraft text in here results in fucked textures.
                // Even if you revert the GL state to exactly what it was before rendering imgui.
                // So we just toss the text we want to render onto a stack, and we'll draw it after imgui's done.
                KamiHud.postDraw {
                    val scale = KamiHud.getScale()
                    val x = cmd.clipRect.x / scale + 4
                    var y = cmd.clipRect.y / scale + 4
                    var xOffset = 0f
                    for (compiled in text) {
                        for (command in compiled.parts) {
                            if (command.multiline) {
                                val codes = command.codes
                                var lastWidth = 0f
                                command.toString().split("\n").forEach {
                                    val width = if (command.shadow)
                                        Wrapper.getMinecraft().textRenderer.drawWithShadow(
                                            codes + it,
                                            x + xOffset,
                                            y,
                                            command.currentColourRGB()
                                        ) - (x + xOffset)
                                    else
                                        Wrapper.getMinecraft().textRenderer.draw(
                                            codes + it,
                                            x + xOffset,
                                            y,
                                            command.currentColourRGB()
                                        ) - (x + xOffset)
                                    lastWidth = width
                                    y += Wrapper.getMinecraft().textRenderer.fontHeight + 4
                                }
                                xOffset += lastWidth
                                y -= Wrapper.getMinecraft().textRenderer.fontHeight + 4
                            } else {
                                val str = command.codes + command // toString is called here -> supplier.get()
                                val width = if (command.shadow)
                                    Wrapper.getMinecraft().textRenderer.drawWithShadow(
                                        str,
                                        x + xOffset,
                                        y,
                                        command.currentColourRGB()
                                    ) - (x + xOffset)
                                else
                                    Wrapper.getMinecraft().textRenderer.draw(
                                        str,
                                        x + xOffset,
                                        y,
                                        command.currentColourRGB()
                                    ) - (x + xOffset)
                                xOffset += width
                            }
                        }
                        xOffset = 0f
                        y += Wrapper.getMinecraft().textRenderer.fontHeight + 4
                    }
                }
            })
        } else {
            var empty = guiOpen
            for (compiled in text) {
                var same = false
                for (part in compiled.parts) {
                    // imgui wants agbr colours
                    pushStyleColor(Col.Text, part.currentColour())
                    if (same) sameLine(spacing = 0f)
                    else same = true
                    val str = part.toString()
                    val notBlank = str.isNotBlank()
                    if (empty && notBlank) empty = false
                    if (notBlank)
                        text(str)
                    popStyleColor()
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
        val guiOpen = Wrapper.getMinecraft().currentScreen is KamiGuiScreen

        if (guiOpen && editWindow) {
            editWindow()
        }

        if (minecraftFont && !guiOpen && text.isNotEmpty()) {
            val scale = KamiHud.getScale()

            val width = (text.map {
                it.parts.sumBy { part ->
                    if (part.multiline)
                        part.toString().split("\n").map { slice -> Wrapper.getMinecraft().textRenderer.getStringWidth(slice) }.max() ?: 0
                    else
                        Wrapper.getMinecraft().textRenderer.getStringWidth(part.toString())
                }
            }.max()?.times(scale) ?: 0) + 24
            val lines = (text.map {
                it.parts.map { part -> if (part.multiline) part.toString().split("\n").size - 1 else 0 }.sum() + 1
            }).sum()
            val height = (Wrapper.getMinecraft().textRenderer.fontHeight + 4) * scale * lines + 8
            setNextWindowSize(Vec2(width, height))
        }
    }

    private fun editWindow() {
        window("Edit $title", ::editWindow) {
            fun setEditPart(part: CompiledText.Part) {
                editPart = part
                editColourComboIndex = if (part.rainbow) 1 else 0
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
                parts.forEachIndexed { n, part ->
                    val highlight = editPart == part
                    if (highlight) {
                        pushStyleColor(Col.Text, (style.colors[Col.Text.i] / 1.2f))
                    }
                    button("$part###part-button-${part.hashCode()}") {
                        setEditPart(part)
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
                        acceptDragDropPayload(PAYLOAD_TYPE_COLOR_3F)?.let {
                            part.colour = it.data!! as Vec4
                        }
                    }
                    sameLine() // The next button should be on the same line
                    if (highlight) {
                        popStyleColor()
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
                    menuItem("Variable") { addPart(CompiledText.VariablePart(CompiledText.ConstantVariable(string = "No variable selected"))) }
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
            separator()
            editPart?.let {
                val col = it.colour
                combo("Colour mode", ::editColourComboIndex, "Static${NUL}Rainbow") {
                    it.rainbow = editColourComboIndex == 1
                }
                if (editColourComboIndex == 0) {
                    if (colorEditVec4("Colour", col, flags = ColorEditFlag.NoAlpha.i)) {
                        it.colour = col
                    }
                }

                it.edit(variableMap)

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

    private fun getVariable(selected: String): CompiledText.Variable {
        val f: () -> CompiledText.Variable = variableMap[selected] ?: error("Invalid item selected")
        return f()
    }

    override fun fillStyle() {
        super.fillStyle()
        checkbox("Minecraft font", ::minecraftFont) {}
        sameLine()
        demoDebugInformations.helpMarker("Only visible when GUI is closed.")
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
            var rainbow: Boolean = false,
            var extraspace: Boolean = true
        ) {
            private fun toCodes(): String {
                return (if (obfuscated) "§k" else "") +
                        (if (bold) "§l" else "") +
                        (if (strike) "§m" else "") +
                        (if (underline) "§n" else "") +
                        (if (italic) "§o" else "")
            }

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

            private fun Vec4.toRGB(): Int {
                val r = (x * 255.0F).toInt()
                val g = (y * 255.0F).toInt()
                val b = (z * 255.0F).toInt()
                return (r shl 16) or (g shl 8) or b
            }

            var colour: Vec4 = Vec4(1.0f, 1.0f, 1.0f, 1.0f)
                set(value) {
                    field = value
                    rgb = colour.toRGB()
                }
            var rgb: Int = this.colour.toRGB()

            override fun toString(): String {
                return if (extraspace) " " else ""
            }

            fun currentColour(): Vec4 {
                return if (rainbow) KamiMod.rainbow.vec4 else colour
            }

            fun currentColourRGB(): Int {
                return if (rainbow) KamiMod.rainbow else rgb
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
            rainbow: Boolean = false,
            extraspace: Boolean = true
        ) : Part(obfuscated, bold, strike, underline, italic, shadow, rainbow, extraspace) {
            override val multiline = false

            override fun toString(): String {
                return string + super.toString()
            }

            override fun edit(variableMap: Map<String, () -> Variable>) {
                val buf = string.toByteArray(ByteArray(string.length + 2))
                if (inputText("Text", buf)) {
                    var str = ""
                    for (c in buf) {
                        if (c != 0.toByte())
                            str += c.toChar()
                        else
                            break
                    }
                    string = str
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
            rainbow: Boolean = false,
            extraspace: Boolean = true
        ) : Part(obfuscated, bold, strike, underline, italic, shadow, rainbow, extraspace) {
            private var editVarComboIndex = 0
            private var editDigits = 0

            override val multiline: Boolean
                get() = variable.multiline

            override fun toString(): String {
                return variable.provide() + super.toString()
            }

            override fun edit(variableMap: Map<String, () -> Variable>) {
                combo("Variable", ::editVarComboIndex, variableMap.keys.joinToString(0.toChar().toString())) {
                    val selected: String = variableMap.keys.toList()[editVarComboIndex]
                    val v = getVariable(selected)
                    if (v is NumericalVariable) {
                        v.digits = editDigits
                    }
                    variable = v
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
            }
        }

        abstract class Variable {
            abstract val multiline: Boolean

            abstract fun provide(): String
        }

        class ConstantVariable(_multiline: Boolean = false, private val string: String) : Variable() {
            override val multiline = _multiline

            override fun provide(): String {
                return string
            }
        }

        class NumericalVariable(private val provider: () -> Double, var digits: Int = 0) : Variable() {
            override val multiline = false

            override fun provide(): String {
                val number = provider()
                return String.format("%.${digits}f", number)
            }
        }

        class StringVariable(_multiline: Boolean = false, private val provider: () -> String) : Variable() {
            override val multiline = _multiline

            override fun provide(): String {
                return provider()
            }
        }

    }

}