package me.zeroeightsix.kami.gui.windows

import glm_.vec2.Vec2
import imgui.ImGui
import imgui.WindowFlag
import imgui.dsl.button
import imgui.dsl.window
import me.zeroeightsix.kami.gui.inputText
import me.zeroeightsix.kami.gui.inputTextMultiline
import me.zeroeightsix.kami.gui.withId

object Macros {

    private var newMacroName = ""
    private var open = false

    private const val TEMPLATE = """function main()
  print("hello world!")
end

main()"""
    private val macros = mutableListOf<Macro>()

    operator fun invoke() = with(ImGui) {
        window("Macros", ::open) {
            text("Create macro")
            sameLine()
            setNextItemWidth(200f)
            inputText("Name", ::newMacroName)
            sameLine()
            button("+") {
                macros += Macro(newMacroName)
            }

            separator()

            if (macros.isEmpty()) {
                textDisabled("No macros yet.")
                return@with
            }

            macros.removeIf {
                withId(it) {
                    button(">") // TODO: Actual run button
                    sameLine()
                    text(it.name)
                    sameLine()
                    button("Edit") {
                        it.editor = Macro.Editor(it.source)
                    }
                    sameLine()
                    button("Delete")
                }
            }
        }

        macros.forEach {
            val editor = it.editor ?: return@forEach
            withId(it) {
                open = true
                window("Editing `${it.name}`", ::open) {
                    inputTextMultiline("input", editor::text, Vec2(-Float.MIN_VALUE, textLineHeight * 16f))
                }
                if (!open) it.editor = null
            }
        }
    }

    private data class Macro(val name: String, val source: String = TEMPLATE, var editor: Editor? = null) {
        data class Editor(var text: String)
    }

}