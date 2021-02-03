package me.zeroeightsix.kami.gui.widgets

import imgui.ImGui
import imgui.ImGui.setNextWindowSize
import me.zeroeightsix.kami.gui.ImguiDSL.calcTextSize
import me.zeroeightsix.kami.gui.ImguiDSL.cursorPosX
import me.zeroeightsix.kami.gui.KamiGuiScreen
import me.zeroeightsix.kami.gui.KamiHud
import me.zeroeightsix.kami.gui.KamiImgui
import me.zeroeightsix.kami.mc
import me.zeroeightsix.kami.setting.GenerateType

@GenerateType
class InventoryPinnableWidget(
    name: String,
    position: Position = Position.TOP_LEFT,
    open: Boolean = true,
    pinned: Boolean = true,
    background: Boolean = false
) : PinnableWidget(name, position, open, pinned, background) {

    override fun preWindow() {
        val scale = KamiHud.getScale()
        setNextWindowSize(9 * 16 * scale + 4 * scale, 3 * 16 * scale + 4 * scale)
    }

    override fun fillWindow() {
        val windowPosX = ImGui.getWindowPosX()
        val windowPosY = ImGui.getWindowPosY()
        val windowWidth = ImGui.getWindowWidth()
        if (mc.currentScreen is KamiGuiScreen) {
            // If the GUI is displayed, don't render the inventory.
            // This is because we can't render minecraft stuff while imgui is rendering (will screw up textures)
            val text = "Inventory overlay"
            val width = calcTextSize(text).x
            cursorPosX = (windowWidth - width).coerceAtLeast(0f) * 0.5f
            ImGui.textDisabled(text)
        } else {
            val scale = KamiHud.getScale()
            KamiImgui.postDraw {
                val item = mc.itemRenderer ?: return@postDraw
                val player = mc.player ?: return@postDraw

                val baseX = (windowPosX / scale).toInt()
                val baseY = (windowPosY / scale).toInt()
                // render inventory
                (1..4).forEach { row ->
                    (0 until 9).forEach { column ->
                        val slot = row * 9 + column
                        val stack = player.inventory.getStack(slot)
                        val isEmpty = stack.isEmpty
                        if (!isEmpty) {
                            val x = baseX + column * 16 + 2
                            val y = baseY + (row - 1) * 16 + 2
                            item.renderInGui(
                                stack,
                                x,
                                y
                            )
                            item.renderGuiItemOverlay(
                                mc.textRenderer,
                                stack,
                                x,
                                y
                            )
                        }
                    }
                }
            }
        }
    }
}
