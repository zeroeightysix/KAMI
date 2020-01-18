package me.zeroeightsix.kami.gui.widgets

import glm_.vec2.Vec2
import glm_.vec4.Vec4
import imgui.Col
import imgui.ColorEditFlag
import imgui.ImGui
import imgui.ImGui.colorEditVec4
import imgui.ImGui.currentWindow
import imgui.ImGui.dragInt
import imgui.ImGui.dummy
import imgui.ImGui.inputText
import imgui.ImGui.openPopup
import imgui.ImGui.popStyleColor
import imgui.ImGui.pushStyleColor
import imgui.ImGui.sameLine
import imgui.ImGui.separator
import imgui.ImGui.setNextWindowSize
import imgui.ImGui.style
import imgui.ImGui.text
import imgui.ImGui.textDisabled
import imgui.NUL
import imgui.api.demoDebugInformations
import imgui.dsl.button
import imgui.dsl.checkbox
import imgui.dsl.combo
import imgui.dsl.menu
import imgui.dsl.menuItem
import imgui.dsl.popupContextItem
import imgui.dsl.window
import me.zeroeightsix.kami.gui.KamiGuiScreen
import me.zeroeightsix.kami.gui.KamiHud
import me.zeroeightsix.kami.util.LagCompensator
import me.zeroeightsix.kami.util.Wrapper
import kotlin.reflect.KMutableProperty0

open class TextPinnableWidget(private val title: String,
                              private val variableMap: Map<String, () -> CompiledText.Variable> = extendStd(mapOf()),
                              private var text: MutableList<CompiledText> = mutableListOf(CompiledText())) : PinnableWidget(title) {

    private var minecraftFont = false

    private var editWindow = false
    private var editPart: CompiledText.Part? = null
    private var editColourComboIndex = 0
    private var editVarComboIndex = 0
    private var editCharBuf = "Text".toCharArray(CharArray(512))
    private var editDigits = 0

    companion object {
        private var sVarMap: Map<String, () -> CompiledText.Variable>? = null

        internal fun extendStd(extra: Map<String, () -> CompiledText.Variable>): Map<String, () -> CompiledText.Variable> {
            val std = mutableMapOf(
                Pair("none", { CompiledText.ConstantVariable("No variable selected")}),
                Pair("x", { CompiledText.NumericalVariable({ Wrapper.getPlayer().pos.x }, 0) }),
                Pair("y", { CompiledText.NumericalVariable({ Wrapper.getPlayer().pos.y }, 0) }),
                Pair("z", { CompiledText.NumericalVariable({ Wrapper.getPlayer().pos.z }, 0) }),
                Pair("yaw", { CompiledText.NumericalVariable({ Wrapper.getPlayer().yaw.toDouble() }, 0) }),
                Pair("pitch", { CompiledText.NumericalVariable({ Wrapper.getPlayer().pitch.toDouble() }, 0) }),
                Pair("tps", { CompiledText.NumericalVariable({ LagCompensator.INSTANCE.tickRate.toDouble() }, 0)}),
                Pair("username", { CompiledText.ConstantVariable(Wrapper.getMinecraft().session.username) })
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
                            val str = command.codes + command // toString is called here -> supplier.get()
                            val width = Wrapper.getMinecraft().textRenderer.draw(str, x + xOffset, y, command.rgb) - (x + xOffset)
                            xOffset += width
                        }
                        xOffset = 0f
                        y += Wrapper.getMinecraft().textRenderer.fontHeight + 4
                    }
                }
            })
        } else {
            for (compiled in text) {
                var same = false
                for (part in compiled.parts) {
                    // imgui wants agbr colours
                    pushStyleColor(Col.Text, part.colour)
                    if (same) sameLine(spacing = 0f)
                    else same = true
                    text(part.toString())
                    popStyleColor()
                }
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
                val str = it.toString()
                Wrapper.getMinecraft().textRenderer.getStringWidth(str)
            }.max()?.times(scale) ?: 0) + 24
            val height = (Wrapper.getMinecraft().textRenderer.fontHeight + 4) * scale * text.size + 8
            setNextWindowSize(Vec2(width, height))
        }
    }

    private fun editWindow() {
        window("Edit $title", ::editWindow) {
            fun setEditPart(part: CompiledText.Part) {
                editPart = part
                when (part) {
                    is CompiledText.LiteralPart -> {
                        editCharBuf = part.string.toCharArray(CharArray(128))
                    }
                    is CompiledText.VariablePart -> {
                        when (val variable = part.variable) {
                            is CompiledText.NumericalVariable -> {
                                editDigits = variable.digits
                            }
                        }
                    }
                }
            }

            if (text.isEmpty()) {
                button("New line") {
                    text.add(CompiledText())
                }
            }

            val iterator = text.listIterator()
            var index = 0
            for (compiled in iterator) {
                for (part in compiled.parts) {
                    val highlight = editPart == part
                    if (highlight) {
                        pushStyleColor(Col.Text, (style.colors[Col.Text.i] / 1.2f))
                    }
                    button("$part###part-button-${part.hashCode()}") {
                        setEditPart(part)
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
                    menuItem("Variable") { addPart(CompiledText.VariablePart(CompiledText.ConstantVariable("No variable selected"))) }
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
                combo("Colour mode", ::editColourComboIndex, "Static${NUL}Rainbow") {}
                if (editColourComboIndex == 0) {
                    if (colorEditVec4("Colour", col, flags = ColorEditFlag.NoAlpha.i)) {
                        it.colour = col
                    }
                }

                when (it) {
                    is CompiledText.LiteralPart -> {
                        if (inputText("Text", editCharBuf)) {
                            var str = ""
                            for (c in editCharBuf) {
                                if (c != 0.toChar())
                                    str += c
                                else
                                    break
                            }
                            it.string = str
                            sameLine()
                            text("+")
                            sameLine()
                            val space = booleanArrayOf(it.extraspace)
                            if (ImGui.checkbox("Space", space)) {
                                it.extraspace = space[0]
                            }
                        }
                    }
                    is CompiledText.VariablePart -> {
                        combo("Variable", ::editVarComboIndex, variableMap.keys.joinToString(0.toChar().toString())) {
                            val selected: String = variableMap.keys.toList()[editVarComboIndex]
                            val v = getVariable(selected)
                            if (v is CompiledText.NumericalVariable) {
                                v.digits = editDigits
                            }
                            it.variable = v
                        }
                        sameLine()
                        text("+")
                        sameLine()
                        val space = booleanArrayOf(it.extraspace)
                        if (ImGui.checkbox("Space", space)) {
                            it.extraspace = space[0]
                        }
                        when (val variable = it.variable) {
                            is CompiledText.NumericalVariable -> {
                                if (dragInt("Digits", ::editDigits, vSpeed = 0.1f, vMin = 0, vMax = 8)) {
                                    variable.digits = editDigits
                                }
                            }
                        }
                    }
                }

                if (minecraftFont) {
                    val bold = booleanArrayOf(it.bold)
                    val italic = booleanArrayOf(it.italic)
                    val underline = booleanArrayOf(it.underline)
                    val strikethrough = booleanArrayOf(it.strike)
                    val obfuscated = booleanArrayOf(it.obfuscated)
                    if (ImGui.checkbox("Bold", bold)) it.bold = !it.bold
                    sameLine()
                    if (ImGui.checkbox("Italic", italic)) it.italic = !it.italic
                    sameLine()
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

    internal fun getVariable(selected: String): CompiledText.Variable {
        val f: () -> CompiledText.Variable = variableMap[selected] ?: error("Invalid item selected")
        val v = f()
        return v
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
        var parts: List<Part> = listOf()
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
            val rainbow: Boolean = false,
            var extraspace: Boolean = true
        ) {
            private fun toCodes(): String {
                return  (if (obfuscated) "§k" else "") +
                        (if (bold) "§l" else "") +
                        (if (strike) "§m" else "") +
                        (if (underline) "§n" else "") +
                        (if (italic) "§o" else "")
            }

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
                    rgb = colour.toRGB()
                    field = value
                }
            var rgb: Int = this.colour.toRGB()

            override fun toString(): String {
                return if (extraspace) " " else ""
            }
        }
        
        class LiteralPart(
            var string: String,
            obfuscated: Boolean = false,
            bold: Boolean = false,
            strike: Boolean = false,
            underline: Boolean = false,
            italic: Boolean = false,
            rainbow: Boolean = false,
            extraspace: Boolean = true
        ) : Part(obfuscated, bold, strike, underline, italic, rainbow, extraspace) {
            override fun toString(): String {
                return string + super.toString()
            }
        }

        class VariablePart(
            var variable: Variable,
            obfuscated: Boolean = false,
            bold: Boolean = false,
            strike: Boolean = false,
            underline: Boolean = false,
            italic: Boolean = false,
            rainbow: Boolean = false,
            extraspace: Boolean = true
        ): Part(obfuscated, bold, strike, underline, italic, rainbow, extraspace) {
            override fun toString(): String {
                return variable.provide() + super.toString()
            }
        }

        abstract class Variable {
            abstract fun provide(): String
        }
        
        class ConstantVariable(private val string: String): Variable() {
            override fun provide(): String {
                return string
            }
        }

        class NumericalVariable(private val provider: () -> Double, var digits: Int = 0): Variable() {
            override fun provide(): String {
                val number = provider()
                return String.format("%.${digits}f", number)
            }
        }
        
        class StringVariable(private val provider: () -> String): Variable() {
            override fun provide(): String {
                return provider()
            }
        }

    }

}