package me.zeroeightsix.kami.gui.widgets

import glm_.vec2.Vec2
import glm_.vec4.Vec4
import imgui.Col
import imgui.ColorEditFlag
import imgui.ImGui
import imgui.ImGui.colorEditVec4
import imgui.ImGui.currentWindow
import imgui.ImGui.dummy
import imgui.ImGui.openPopup
import imgui.ImGui.popStyleColor
import imgui.ImGui.pushStyleColor
import imgui.ImGui.sameLine
import imgui.ImGui.separator
import imgui.ImGui.setNextWindowSize
import imgui.ImGui.style
import imgui.ImGui.text
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
import me.zeroeightsix.kami.util.Wrapper
import kotlin.reflect.KMutableProperty0

open class TextPinnableWidget(private val title: String) : PinnableWidget(title) {

    private var minecraftFont = false
    private var text: MutableList<CompiledText> = mutableListOf(CompiledText())
    
    private var editWindow = false
    private var editPart: CompiledText.Part? = null
    private var editComboIndex = 0
    
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
            val height = (Wrapper.getMinecraft().textRenderer.fontHeight) * scale * text.size + 16
            setNextWindowSize(Vec2(width, height))
        }
    }

    private fun editWindow() {
        window("Edit $title", ::editWindow) {
            val windowVisibleX2 = ImGui.windowPos.x + ImGui.windowContentRegionMax.x
            if (text.isEmpty()) {
                button("New line") {
                    text.add(CompiledText())
                }
            }

            val iterator = text.listIterator()
            var index = 0
            for (compiled in iterator) {
                for (part in compiled.parts) {
                    button(part.toString()) {
                        editPart = part
                    }
                    val lastButtonX2 = ImGui.itemRectMax.x
                    val nextButtonX2 = lastButtonX2 + ImGui.style.itemSpacing.x // Expected position if next button was on same line
                    if (nextButtonX2 < windowVisibleX2)
                        sameLine()
                }
                pushStyleColor(Col.Button, style.colors[Col.Button.i] * 0.7f)
                button("+###plus-button-$index") {
                    openPopup("plus-popup-$index")
                }
                popupContextItem("plus-popup-$index") {
                    menuItem("Text") {
                        val mutable = compiled.parts.toMutableList()
                        mutable.add(CompiledText.LiteralPart("Text"))
                        compiled.parts = mutable
                    }
                    menuItem("Variable") {

                    }
                    if (index != 0) {
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
                    } else {
                        menuItem("Line") {
                            iterator.add(CompiledText())
                        }
                    }
                }

                sameLine(spacing = 4f)
                button("-###minus-button-$index") {
                    iterator.remove()
                }
                popStyleColor()
                
                index++
            }
            dummy(Vec2(0, 0))
            separator()
            editPart?.let {
                val col = it.colour
                combo("Colour mode", ::editComboIndex, "Static${NUL}Rainbow") {}
                if (editComboIndex == 0) {
                    if (colorEditVec4("Colour", col, flags = ColorEditFlag.NoAlpha.i)) {
                        it.colour = col
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
            obfuscated: Boolean = false,
            bold: Boolean = false,
            strike: Boolean = false,
            underline: Boolean = false,
            italic: Boolean = false,
            val rainbow: Boolean = false
        ) {
            val codes: String = (if (obfuscated) "§k" else "") +
                    (if (bold) "§l" else "") +
                    (if (strike) "§m" else "") +
                    (if (underline) "§n" else "") +
                    (if (italic) "§o" else "")
            
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
            
            abstract override fun toString(): String
        }
        
        class LiteralPart(
            val string: String,
            obfuscated: Boolean = false,
            bold: Boolean = false,
            strike: Boolean = false,
            underline: Boolean = false,
            italic: Boolean = false,
            rainbow: Boolean = false
        ) : Part(obfuscated, bold, strike, underline, italic, rainbow) {
            override fun toString(): String {
                return string
            }
        }
        
        class VariablePart(
            val function: (Part) -> String,
            obfuscated: Boolean = false,
            bold: Boolean = false,
            strike: Boolean = false,
            underline: Boolean = false,
            italic: Boolean = false,
            rainbow: Boolean = false,
            digits: Int = 0
        ): Part(obfuscated, bold, strike, underline, italic, rainbow) {
            override fun toString(): String {
                return function(this)
            }
        }

    }

}