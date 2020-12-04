package me.zeroeightsix.kami.gui.windows

import imgui.ImGui
import imgui.dsl.button
import imgui.dsl.window
import me.zeroeightsix.kami.filterNonNull
import me.zeroeightsix.kami.gui.inputTextMultiline
import me.zeroeightsix.kami.gui.withId

object Macros {

    private val macros = mutableListOf<Macro>(
        // Macro("print(\"Hello world!\")")
    )

    operator fun invoke() = with(ImGui) {
        if (macros.isEmpty()) {
            text("No macros yet! Create one?")
        } else {
            text("Create new macro")
        }
        sameLine()

        button("+") {
            macros += Macro("Macro")
        }

        separator()

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

        macros.stream().map { it.editor }.filterNonNull().forEach {
            withId(it) {
                window("Macro editor") {
                    inputTextMultiline("input", it::text, windowContentRegionMax)
                }
            }
        }
    }

    private data class Macro(val name: String, val source: String = "", var editor: Editor? = null) {
        data class Editor(var text: String)
    }

}