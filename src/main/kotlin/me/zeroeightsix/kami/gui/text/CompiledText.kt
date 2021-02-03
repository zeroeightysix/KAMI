package me.zeroeightsix.kami.gui.text

import imgui.ImGui
import imgui.flag.ImGuiCol
import imgui.flag.ImGuiColorEditFlags
import imgui.flag.ImGuiMouseButton
import imgui.type.ImString
import me.zeroeightsix.kami.Colour
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.conditionalWrap
import me.zeroeightsix.kami.cyclingIterator
import me.zeroeightsix.kami.forEachRemainingIndexed
import me.zeroeightsix.kami.gui.ImguiDSL.PAYLOAD_TYPE_COLOR_4F
import me.zeroeightsix.kami.gui.ImguiDSL.button
import me.zeroeightsix.kami.gui.ImguiDSL.checkbox
import me.zeroeightsix.kami.gui.ImguiDSL.colors
import me.zeroeightsix.kami.gui.ImguiDSL.combo
import me.zeroeightsix.kami.gui.ImguiDSL.dragDropTarget
import me.zeroeightsix.kami.gui.ImguiDSL.imgui
import me.zeroeightsix.kami.gui.ImguiDSL.menuItem
import me.zeroeightsix.kami.gui.ImguiDSL.popupContextItem
import me.zeroeightsix.kami.gui.ImguiDSL.withStyleColour
import me.zeroeightsix.kami.gui.ImguiDSL.wrapImBool
import me.zeroeightsix.kami.gui.ImguiDSL.wrapImInt
import me.zeroeightsix.kami.gui.ImguiDSL.wrapSingleIntArray

class CompiledText(
    var parts: MutableList<Part> = mutableListOf()
) {

    var selectedPart: Part? = null

    /**
     * @return `true` if mutated
     */
    fun edit(
        id: String,
        highlightSelected: Boolean = false,
        selectedAction: (Part) -> Unit = { },
        plusButtonExtra: () -> Unit = {}
    ): Boolean {
        var dirty = false
        parts.listIterator().let { iterator ->
            iterator.forEachRemainingIndexed { n, part ->
                val highlight = highlightSelected && part == selectedPart

                highlight.conditionalWrap(
                    {
                        val color = ImGui.getStyle().colors[ImGuiCol.Text]
                        ImGui.pushStyleColor(
                            ImGuiCol.Text,
                            color[0] / 1.2f,
                            color[1] / 1.2f,
                            color[2] / 1.2f,
                            color[3] / 1.2f
                        )
                    },
                    {
                        val buttonLabel = "${part.editLabel}###part-button-$id-$n"
                        button(buttonLabel) {
                            this.selectedPart = part
                        }

                        // Set colour if colour dropped on this button
                        dragDropTarget {
                            ImGui.acceptDragDropPayload(PAYLOAD_TYPE_COLOR_4F)?.let {
                                part.colour =
                                    Colour(it[0].toFloat(), it[1].toFloat(), it[2].toFloat(), it[3].toFloat())
                            }
                        }

                        // Drag to move item
                        if (ImGui.isItemActive() && !ImGui.isItemHovered()) {
                            val nNext = n + if (ImGui.getMouseDragDeltaX(ImGuiMouseButton.Left) < 0f) -1 else 1
                            if (nNext in parts.indices) {
                                parts[n] = parts[nNext]
                                parts[nNext] = part
                                ImGui.resetMouseDragDelta()
                            }
                        }

                        popupContextItem(buttonLabel) {
                            menuItem("Remove") {
                                // Remove this part from the list
                                iterator.remove()
                                dirty = true
                            }
                            selectedAction(part)
                        }

                        ImGui.sameLine() // The next button should be on the same line
                    },
                    {
                        ImGui.popStyleColor()
                    }
                )
            }

            val color = ImGui.getStyle().colors[ImGuiCol.Button]
            withStyleColour(ImGuiCol.Button, color[0] * 0.7f, color[1] * 0.7f, color[2] * 0.7f, color[3] * 0.7f) {
                button("+###plus-button-$id") {
                    ImGui.openPopup("plus-popup-$id")
                }
                popupContextItem("plus-popup-$id") {
                    fun addPart(part: Part) {
                        this.parts = parts.toMutableList().also {
                            it.add(part)
                            this.selectedPart = part
                            dirty = true
                        }
                    }
                    menuItem("Text") { addPart(LiteralPart("Text".imgui)) }
                    menuItem("Variable") { addPart(VariablePart(VarMap["none"]!!())) }
                    plusButtonExtra()
                }
            }
        }
        return dirty
    }

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
        var extraSpace: Boolean = true
    ) {
        private var editColourComboIndex = ColourMode.values().indexOf(this.colourMode)

        enum class FormattingEditMode {
            ABSENT, DISABLED, ENABLED
        }

        fun edit(
            colour: Boolean = false,
            formatting: FormattingEditMode = FormattingEditMode.ABSENT
        ) {
            if (colour) {
                val col = this.colour.asFloatRGBA()
                wrapImInt(::editColourComboIndex) {
                    combo(
                        "Colour mode",
                        it,
                        if (this.multiline) ColourMode.listMultiline else ColourMode.listNormal
                    ) {
                        this.colourMode = ColourMode.values()[it.get()]
                    }
                }

                when (this.colourMode) {
                    ColourMode.STATIC -> {
                        if (ImGui.colorEdit4("Colour", col, ImGuiColorEditFlags.AlphaBar)) {
                            this.colour = Colour.fromFloatRGBA(col)
                        }
                    }
                    ColourMode.ALTERNATING -> {
                        this.colours.forEachIndexed { i, vec ->
                            ImGui.colorEdit4("Colour $i", vec.asFloatRGBA(), ImGuiColorEditFlags.AlphaBar)
                        }

                        // TODO: Allow colours to be added / removed
                    }
                    else -> Unit
                }
            }

            this.editValue(VarMap.inner)

            when (formatting) {
                FormattingEditMode.ABSENT -> Unit
                FormattingEditMode.DISABLED -> {
                    ImGui.textDisabled("Styles disabled")
                    if (ImGui.isItemHovered()) {
                        ImGui.beginTooltip()
                        ImGui.pushTextWrapPos(ImGui.getFontSize() * 35f)
                        ImGui.text("Enable minecraft font rendering to enable styles")
                        ImGui.popTextWrapPos()
                        ImGui.endTooltip()
                    }
                }
                FormattingEditMode.ENABLED -> {
                    if (ImGui.checkbox("Shadow", shadow)) shadow = !shadow
                    ImGui.sameLine()
                    if (ImGui.checkbox("Bold", bold)) bold = !bold
                    ImGui.sameLine()
                    if (ImGui.checkbox("Italic", italic)) italic = !italic
                    // newline
                    if (ImGui.checkbox("Underline", underline)) underline = !underline
                    ImGui.sameLine()
                    if (ImGui.checkbox("Cross out", strike)) strike = !strike
                    ImGui.sameLine()
                    if (ImGui.checkbox("Obfuscated", obfuscated)) obfuscated = !obfuscated
                }
            }
        }

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

        private fun FloatArray.toARGB(): Int {
            val r = (this[0] * 255.0F).toInt()
            val g = (this[1] * 255.0F).toInt()
            val b = (this[2] * 255.0F).toInt()
            val a = (this[3] * 255.0F).toInt()
            return (a shl 24) or (r shl 16) or (g shl 8) or b
        }

        // Static colour
        var colour = Colour(1.0f, 1.0f, 1.0f, 1.0f)

        // Alternating colour
        var colours = mutableListOf(
            Colour(1.0f, 1.0f, 1.0f, 1.0f),
            Colour(1.0f, 0.5f, 0.5f, 0.5f)
        )
        val coloursIterator = colours.cyclingIterator()

        override fun toString(): String {
            return if (extraSpace) " " else ""
        }

        fun currentColour(): Colour {
            return when (colourMode) {
                ColourMode.STATIC -> colour
                ColourMode.RAINBOW -> KamiMod.rainbow
                ColourMode.ALTERNATING -> coloursIterator.next()
            }
        }

        fun currentColourARGB(): Int {
            return currentColour().asARGB()
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
                val listNormal = values().filter { !it.multilineExclusive }
                    .map { it.name.toLowerCase().capitalize() }
                val listMultiline = values().map { it.name.toLowerCase().capitalize() }
            }
        }

        abstract fun editValue(variableMap: Map<String, () -> Variable>)
    }

    class LiteralPart(
        var string: ImString,
        obfuscated: Boolean = false,
        bold: Boolean = false,
        strike: Boolean = false,
        underline: Boolean = false,
        italic: Boolean = false,
        shadow: Boolean = true,
        colourMode: ColourMode = ColourMode.STATIC,
        extraSpace: Boolean = true
    ) : Part(obfuscated, bold, strike, underline, italic, shadow, colourMode, extraSpace) {
        override val multiline = false

        override fun toString(): String {
            return string.get() + super.toString()
        }

        override fun editValue(variableMap: Map<String, () -> Variable>) {
            ImGui.inputText("Text", string)

            ImGui.sameLine()
            ImGui.text("+")
            ImGui.sameLine()
            wrapImBool(extraSpace) { bool ->
                checkbox("Space", bool)
                extraSpace = bool.get()
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
        extraSpace: Boolean = true
    ) : Part(obfuscated, bold, strike, underline, italic, shadow, colourMode, extraSpace) {
        private var editVarComboIndex = -1
        private var editDigits = if (variable is NumericalVariable) {
            (variable as NumericalVariable).digits
        } else 0

        override val multiline: Boolean
            get() = variable.multiline

        override val editLabel: String
            get() = variable.editLabel + super.toString()

        override fun toString(): String {
            return variable.provide() + super.toString()
        }

        override fun editValue(variableMap: Map<String, () -> Variable>) {
            if (editVarComboIndex == -1) editVarComboIndex = variableMap.keys.indexOf(this.variable.name)
            combo("Variable", ::editVarComboIndex, variableMap.keys) {
                val selected: String = variableMap.keys.toList()[it.get()]
                val v = (variableMap[selected] ?: error("Invalid item selected")).invoke()
                if (v is NumericalVariable) {
                    v.digits = editDigits
                }
                this.variable = v
            }

            ImGui.sameLine()
            ImGui.text("+")
            ImGui.sameLine()
            wrapImBool(extraSpace) {
                checkbox("Space", it) {
                    this.extraSpace = it.get()
                }
            }
            when (val variable = variable) {
                is NumericalVariable -> {
                    wrapSingleIntArray(::editDigits) {
                        if (ImGui.dragInt("Digits", it, 0.1f, 0f, 8f)) {
                            variable.digits = it[0]
                        }
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
            val number = provideNumber()
            return String.format("%.${digits}f", number)
        }

        fun provideNumber() = provider()
    }

    open class StringVariable(name: String, _multiline: Boolean = false, private val provider: () -> String) :
        Variable(name) {
        override val multiline = _multiline

        override fun provide(): String {
            return provider()
        }
    }
}