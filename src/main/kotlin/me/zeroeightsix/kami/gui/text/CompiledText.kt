package me.zeroeightsix.kami.gui.text

import glm_.vec4.Vec4
import imgui.*
import me.zeroeightsix.kami.*
import kotlin.math.abs
import kotlin.math.floor

class CompiledText(
    var parts: MutableList<Part> = mutableListOf()
) {

    var selectedPart: Part? = null

    fun edit(highlightSelected: Boolean = false) {
        parts.listIterator().let { iterator ->
            iterator.forEachRemainingIndexed { n, part ->
                val highlight = highlightSelected && part == selectedPart

                highlight.conditionalWrap(
                    {
                        ImGui.pushStyleColor(Col.Text, (ImGui.style.colors[Col.Text.i] / 1.2f))
                    }, {
                        dsl.button("${part.editLabel}###part-button-${part.hashCode()}") {
                            this.selectedPart = part
                        }

                        // Set colour if colour dropped on this button
                        dsl.dragDropTarget {
                            ImGui.acceptDragDropPayload(PAYLOAD_TYPE_COLOR_4F)?.let {
                                part.colour = it.data!! as Vec4
                            }
                        }

                        // Drag to move item
                        if (ImGui.isItemActive && !ImGui.isItemHovered()) {
                            val nNext = n + if (ImGui.getMouseDragDelta(MouseButton.Left).x < 0f) -1 else 1
                            if (nNext in parts.indices) {
                                parts[n] = parts[nNext]
                                parts[nNext] = part
                                ImGui.resetMouseDragDelta()
                            }
                        }

                        dsl.popupContextItem {
                            dsl.menuItem("Remove") {
                                // Remove this part from the list
                                iterator.remove()
                            }
                        }

                        ImGui.sameLine() // The next button should be on the same line
                    }, {
                        ImGui.popStyleColor()
                    }
                )
            }
        }
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
                val listNormal = values().filter { !it.multilineExclusive }
                    .joinToString("$NUL") { it.name.toLowerCase().capitalize() }
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
            val buf =
                string.toByteArray(ByteArray((((floor((abs((string.length - 4) / 256) + 1).toDouble()))) * 256).toInt()))
            if (ImGui.inputText("Text", buf)) {
                string = buf.backToString()
            }
            ImGui.sameLine()
            ImGui.text("+")
            ImGui.sameLine()
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
            dsl.combo("Variable", ::editVarComboIndex, variableMap.keys.joinToString(0.toChar().toString())) {
                val selected: String = variableMap.keys.toList()[editVarComboIndex]
                val v = (variableMap[selected] ?: error("Invalid item selected")).invoke()
                if (v is NumericalVariable) {
                    v.digits = editDigits
                }
                this.variable = v
            }
            ImGui.sameLine()
            ImGui.text("+")
            ImGui.sameLine()
            val space = booleanArrayOf(extraspace)
            if (ImGui.checkbox("Space", space)) {
                extraspace = space[0]
            }
            when (val variable = variable) {
                is NumericalVariable -> {
                    if (ImGui.dragInt("Digits", ::editDigits, vSpeed = 0.1f, vMin = 0, vMax = 8)) {
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
