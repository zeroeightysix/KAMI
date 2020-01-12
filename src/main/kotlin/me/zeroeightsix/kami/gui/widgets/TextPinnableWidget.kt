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
import java.util.regex.Pattern
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
        companion object {
            val pattern: Pattern = Pattern.compile("\\\$(\\w+)")
        }
        
        private val parts: List<Supplier<String>>

        init {
            parts = ArrayList()
            val matcher = pattern.matcher(text)
            var index = 0
            
            fun String.diff(start: Int, end: Int? = null): String {
                val diff = when (end) {
                    null -> substring(start)
                    else -> substring(start, end)
                }
                if (diff.startsWith("\\")) {
                    return diff.substring(1)
                }
                return diff
            }
            
            while (matcher.find()) {
                val lastIndex = index
                index = matcher.start()

                val diff = text.diff(lastIndex, index)
                parts.add(Supplier { diff })

                val group = matcher.group(1)
                parts.add(when (group) {
                    "x" -> Supplier { Wrapper.getPlayer().pos.x.toString() }
                    "y" -> Supplier { Wrapper.getPlayer().pos.y.toString() }
                    "z" -> Supplier { Wrapper.getPlayer().pos.z.toString() }
                    "yaw" -> Supplier { Wrapper.getPlayer().yaw.toString() }
                    "pitch" -> Supplier { Wrapper.getPlayer().pitch.toString() }
                    "tps" -> Supplier { LagCompensator.INSTANCE.tickRate.toString() }
                    "username" -> Supplier { Wrapper.getMinecraft().session.username }
                    else -> Supplier { "" }
                })
                
                index = matcher.end()
            }
            val diff = text.diff(index)
            parts.add(Supplier { diff })
        }
        
        override fun toString(): String {
            var str = ""
            for (supplier in parts) {
                str += supplier.get()
            }
            return str
        }
    }
}