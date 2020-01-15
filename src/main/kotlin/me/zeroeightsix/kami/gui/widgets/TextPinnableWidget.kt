package me.zeroeightsix.kami.gui.widgets

import glm_.vec2.Vec2
import imgui.Col
import imgui.ImGui.currentWindow
import imgui.ImGui.popStyleColor
import imgui.ImGui.pushStyleColor
import imgui.ImGui.sameLine
import imgui.ImGui.setNextWindowSize
import imgui.ImGui.text
import imgui.api.demoDebugInformations
import imgui.dsl.checkbox
import imgui.dsl.menuItem
import me.zeroeightsix.kami.gui.KamiGuiScreen
import me.zeroeightsix.kami.gui.KamiHud
import me.zeroeightsix.kami.util.Wrapper
import kotlin.reflect.KMutableProperty0

open class TextPinnableWidget(title: String) : PinnableWidget(title) {

    private var minecraftFont = false
    private var editOpen = false
    private var text: List<CompiledText> = listOf(CompiledText())

    @ExperimentalUnsignedTypes
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
                            val width = Wrapper.getMinecraft().textRenderer.draw(str, x + xOffset, y, command.color) - (x + xOffset)
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
                    pushStyleColor(Col.Text, part.agbr)
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
        if (minecraftFont && !guiOpen && text.isNotEmpty()) {
            val scale = KamiHud.getScale()

            val width = (text.map {
                val str = it.toString()
                Wrapper.getMinecraft().textRenderer.getStringWidth(str)
            }.max()?.times(scale) ?: 0) + 24
            val height = (Wrapper.getMinecraft().textRenderer.fontHeight) * scale * text.size + 16
            setNextWindowSize(Vec2(width, height))
        }
    }

    override fun fillStyle() {
        super.fillStyle()
        checkbox("Minecraft font", ::minecraftFont) {}
        sameLine()
        demoDebugInformations.helpMarker("Only visible when GUI is closed.")
    }

    override fun fillContextMenu() {
        menuItem("Edit") {
            editOpen = true
        }
    }
    
    class CompiledText(
        val parts: List<Part> = listOf()
    ) {

        override fun toString(): String {
            var buf = ""
            for (part in parts) buf += part.toString()
            return buf
        }
        
        data class Part(
            val function: (Part) -> String,
            val color: Int,
            val obfuscated: Boolean = false,
            val bold: Boolean = false,
            val strike: Boolean = false,
            val underline: Boolean = false,
            val italic: Boolean = false,
            val rainbow: Boolean = false,
            val digits: Int = 0
        ) {
            val codes: String = (if (obfuscated) "§k" else "") +
                    (if (bold) "§l" else "") +
                    (if (strike) "§m" else "") +
                    (if (underline) "§n" else "") +
                    (if (italic) "§o" else "")

            val agbr: Int
            
            init {
                val a = 0xFF
                val r = color shr 16 and 0xFF
                val g = color shr 8 and 0xFF
                val b = color and 0xFF
                agbr = (a shl 24) or (b shl 16) or (g shl 8) or r
            }
            
            override fun toString(): String {
                return function(this)
            }
        }

    }

}