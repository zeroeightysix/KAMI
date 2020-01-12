package me.zeroeightsix.kami.gui.widgets

import glm_.vec2.Vec2
import imgui.ImGui.currentWindow
import imgui.ImGui.sameLine
import imgui.ImGui.setNextWindowSize
import imgui.ImGui.text
import imgui.api.demoDebugInformations
import imgui.dsl.checkbox
import me.zeroeightsix.kami.gui.KamiGuiScreen
import me.zeroeightsix.kami.gui.KamiHud
import me.zeroeightsix.kami.util.LagCompensator
import me.zeroeightsix.kami.util.Wrapper
import java.util.function.Supplier
import kotlin.reflect.KMutableProperty0

open class TextPinnableWidget(title: String, val text: ArrayList<CompiledText> = arrayListOf()) : PinnableWidget(title) {

    private var minecraftFont = false
    private val q = arrayListOf<String>()

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
                    for (s in q) {
                        Wrapper.getMinecraft().textRenderer.draw(s, x, y, 0xffffff)
                        y += Wrapper.getMinecraft().textRenderer.fontHeight + 4
                    }
                }
            })
        } else {
            for (s in q) {
                text(s)
            }
        }
    }

    override fun preWindow() {
        q.clear()

        for (compiled in text) {
            val text = compiled.toString()
            q.add(text)
        }
        
        val guiOpen = Wrapper.getMinecraft().currentScreen is KamiGuiScreen
        if (minecraftFont && !guiOpen && text.isNotEmpty()) {
            val scale = KamiHud.getScale()

            val width = (q.map {
                Wrapper.getMinecraft().textRenderer.getStringWidth(it)
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
    
    class CompiledText(text: String) {
        val parts: List<Cmd>

        init {
            parts = text.toParts()
        }

        private fun String.toParts(colour: Int = 0xffffff): List<Cmd> {
            val parts = ArrayList<Cmd>()
            var colour = colour
            var alpha = 0xff

            var buf = ""
            var escape = false

            var obfuscated = false
            var bold = false
            var strike = false
            var underline = false
            var italic = false
            var rainbow = false

            fun pushPart(supplier: Supplier<String>) {
                val cmd = Cmd(
                    supplier,
                    colour,
                    obfuscated, bold, strike, underline, italic, rainbow,
                    alpha
                )
                parts.add(cmd)
            }

            fun pushPart() {
                val str = "" + buf // copy str
                if (str.isEmpty()) return
                pushPart(Supplier { str })
                buf = ""
            }

            fun pushReset() {
                pushPart()

                obfuscated = false
                bold = false
                strike = false
                underline = false
                italic = false
                rainbow = false
            }

            val iterator = iterator()
            while (iterator.hasNext()) {
                val c = iterator.nextChar()
                if (!iterator.hasNext()) { // Any special char is invalid now
                    buf += c
                } else if (escape && (c == '&' || c == '#' || c == '$')) {
                    // We're escaping a special char.
                    // We don't append the backslash.
                    escape = false
                    buf += c
                } else when (c) {
                    '&' -> { // Minecraft colour
                        val next = iterator.nextChar()
                        colour = when (next) {
                            '0' -> { pushReset(); 0x000000 }
                            '1' -> { pushReset(); 0x0000AA }
                            '2' -> { pushReset(); 0x00AA00 }
                            '3' -> { pushReset(); 0x00AAAA }
                            '4' -> { pushReset(); 0xAA0000 }
                            '5' -> { pushReset(); 0xAA00AA }
                            '6' -> { pushReset(); 0xFFAA00 }
                            '7' -> { pushReset(); 0xAAAAAA }
                            '8' -> { pushReset(); 0x555555 }
                            '9' -> { pushReset(); 0x5555FF }
                            'a' -> { pushReset(); 0x55FF55 }
                            'b' -> { pushReset(); 0x55FFFF }
                            'c' -> { pushReset(); 0xFF5555 }
                            'd' -> { pushReset(); 0xFF55FF }
                            'e' -> { pushReset(); 0xFFFF55 }
                            'f' -> { pushReset(); 0xFFFFFF }
                            else -> {
                                pushPart() // We're about to change style, push the last part with the old style.

                                // Oops, we don't actually want to change the colour.
                                // Handle styles and rainbows here.
                                // Leave your pot of gold at the door.
                                when (next) {
                                    'k' -> obfuscated = true
                                    'l' -> bold = true
                                    'm' -> strike = true
                                    'n' -> underline = true
                                    'o' -> italic = true
                                    'r' -> pushReset()
                                    else -> rainbow = true
                                }

                                colour
                            }
                        }
                    }
                    '#' -> { // Hex colour
                        pushPart()

                        var colourBuf = ""
                        loop@ while (iterator.hasNext()) {
                            colourBuf += when (val colourC = iterator.nextChar()) {
                                in '0'..'9' -> colourC
                                in 'a'..'f' -> colourC
                                else -> {
                                    if (colourC != '\\') {
                                        buf += colourC
                                    }
                                    break@loop
                                }
                            }
                        }

                        when (colourBuf.length) {
                            6 -> { // no alpha (format is #rrggbb[aa]
                                colour = Integer.decode("0x$colourBuf")
                            }
                            8 -> {
                                colour = Integer.decode("0x${colourBuf.substring(0..5)}")
                                alpha = Integer.decode("0x${colourBuf.substring(6)}")
                            }
                            else -> {
                                // Invalid format! Ignore this colour.
                            }
                        }
                    }
                    '$' -> { // Variable
                        pushPart()
                        var varBuf = ""
                        loop@ while (iterator.hasNext()) {
                            varBuf += when (val varC = iterator.nextChar()) {
                                in 'a'..'z' -> varC
                                else -> {
                                    if (varC != '\\') {
                                        buf += varC
                                    }
                                    break@loop
                                }
                            }
                        }

                        val supplier = when (varBuf) {
                            "x" -> Supplier { Wrapper.getMinecraft().player.x.toString() }
                            "y" -> Supplier { Wrapper.getMinecraft().player.y.toString() }
                            "z" -> Supplier { Wrapper.getMinecraft().player.z.toString() }
                            "yaw" -> Supplier { Wrapper.getMinecraft().player.yaw.toString() }
                            "pitch" -> Supplier { Wrapper.getMinecraft().player.pitch.toString() }
                            "tps" -> Supplier { LagCompensator.INSTANCE.tickRate.toString() }
                            "username" -> Supplier { Wrapper.getMinecraft().session.username }
                            // TODO: gametime, realtime, gameticks, anticheat
                            else -> Supplier { "unknown variable" }
                        }
                        pushPart(supplier)
                    }
                    '\\' -> { // Escape
                        if (escape) {
                            // escaping an escape
                            buf += '\\'
                        }
                        escape = true
                    }
                    else -> {
                        if (escape) {
                            // We've tried to escape a non-escapable character.
                            // Just toss the backslash and this character onto the buffer.
                            buf += '\\'
                            escape = false
                        }
                        buf += c
                    }
                }
            }

            pushPart() // push remainder
            return parts
        }
        
        data class Cmd(
            val supplier: Supplier<String>,
            val color: Int,
            val obfuscated: Boolean = false,
            val bold: Boolean = false,
            val strike: Boolean = false,
            val underline: Boolean = false,
            val italic: Boolean = false,
            val rainbow: Boolean = false,
            val alpha: Int = 0xFF
        )
    }

}