package me.zeroeightsix.kami.gui.windows

import glm_.vec2.Vec2
import imgui.ImGui
import imgui.WindowFlag
import imgui.dsl.button
import imgui.dsl.menu
import imgui.dsl.menuBar
import imgui.dsl.menuItem
import imgui.dsl.window
import me.zeroeightsix.kami.gui.inputText
import me.zeroeightsix.kami.gui.inputTextMultiline
import me.zeroeightsix.kami.gui.withId

object Macros {

    private var newMacroName = ""
    var open = false

    private const val TEMPLATE = """function main()
  print("hello world!")
end

main()"""
    private val macros = mutableListOf<Macro>()

    operator fun invoke() = with(ImGui) {
        if (!open) return
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
                        it.setEditorOpen()
                    }
                    sameLine()
                    button("Delete")
                }
            }
        }

        macros.forEach {
            withId(it) {
                it.show()
            }
        }
    }

    private data class Macro(val name: String, val source: String = TEMPLATE) {
        private var editor = Editor(this.source)

        fun setEditorOpen() {
            editor.open = true
        }

        fun show() {
            this.editor.show()
        }

        inner class Editor(var text: String) {
            var open = false

            fun show() {
                if (open) {
                    window("Editing `$name`", ::open, flags = WindowFlag.MenuBar.i) {
                        menuBar {
                            menu("Run") {
                                menuItem("Run") {

                                }
                                menuItem("Check") {

                                }
                            }
                        }
                        inputTextMultiline("input", this@Editor::text, Vec2(-Float.MIN_VALUE, -Float.MIN_VALUE))
                    }
                }
            }
        }
    }

}