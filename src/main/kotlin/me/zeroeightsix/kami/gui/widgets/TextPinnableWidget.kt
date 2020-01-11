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
import me.zeroeightsix.kami.util.Wrapper
import kotlin.reflect.KMutableProperty0

open class TextPinnableWidget(title: String) : PinnableWidget(title) {

    var minecraftFont = false
    var text = "I'm a silly goose!"

    override fun fillWindow(open: KMutableProperty0<Boolean>) {

        val guiOpen = Wrapper.getMinecraft().currentScreen is KamiGuiScreen
        // Because of the way minecraft text is rendered, we don't display it when the GUI is open.
        // Otherwise, because it is rendered after imgui, it would always be in the foreground.
        if (minecraftFont && !guiOpen) {
            currentWindow.drawList.addCallback({ list, cmd ->
                // For god knows what reason, rendering minecraft text in here results in fucked textures.
                // Even if you revert the GL state to exactly what it was before rendering imgui.
                // So we just toss the text we want to render onto a stack, and we'll draw it after imgui's done.
                KamiHud.postDraw {
                    val scale = KamiHud.getScale()
                    val x = cmd.clipRect.x / scale + 4
                    val y = cmd.clipRect.y / scale + 4
                    Wrapper.getMinecraft().textRenderer.draw(text, x, y, 0xffffff)
                }
            })
        } else {
            text(text)
        }

    }

    override fun preWindow() {
        val guiOpen = Wrapper.getMinecraft().currentScreen is KamiGuiScreen
        if (minecraftFont && !guiOpen) {
            val scale = KamiHud.getScale()
            val width = (Wrapper.getMinecraft().textRenderer.getStringWidth(text)) * scale + 24
            val height = (Wrapper.getMinecraft().textRenderer.fontHeight) * scale + 16
            setNextWindowSize(Vec2(width, height))
        }
    }

    override fun fillStyle() {
        super.fillStyle()
        checkbox("Minecraft font", ::minecraftFont) {}
        sameLine()
        demoDebugInformations.helpMarker("Only visible when GUI is closed.")
    }

}