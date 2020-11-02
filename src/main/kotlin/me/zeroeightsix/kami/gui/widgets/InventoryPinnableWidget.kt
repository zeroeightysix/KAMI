package me.zeroeightsix.kami.gui.widgets

import com.mojang.blaze3d.platform.GlStateManager
import com.mojang.blaze3d.systems.RenderSystem
import glm_.vec2.Vec2
import imgui.ImGui
import imgui.ImGui.calcTextSize
import imgui.ImGui.currentWindow
import imgui.ImGui.setNextWindowSize
import me.zeroeightsix.kami.gui.KamiGuiScreen
import me.zeroeightsix.kami.gui.KamiHud
import me.zeroeightsix.kami.mc
import me.zeroeightsix.kami.setting.GenerateType
import net.minecraft.client.texture.SpriteAtlasTexture

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
        setNextWindowSize(Vec2(9 * 16 * scale + 4 * scale, 3 * 16 * scale + 4 * scale))
    }

    override fun fillWindow() {
        if (mc.currentScreen is KamiGuiScreen) {
            // If the GUI is displayed, don't render the inventory.
            // This is because we can't render minecraft stuff while imgui is rendering (will screw up textures)
            val text = "Inventory overlay"
            val width = calcTextSize(text).x
            ImGui.cursorPosX = (currentWindow.innerRect.width - width).coerceAtLeast(0f) * 0.5f
            ImGui.textDisabled(text)
        } else {
            val rect = ImGui.currentWindow.rect()
            ImGui.currentWindow.drawList.addCallback({ _, cmd ->
                KamiHud.postDraw {
                    val scale = KamiHud.getScale()

                    mc.textureManager.bindTexture(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE)
                    mc.textureManager.getTexture(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE)!!.setFilter(false, false)

                    RenderSystem.enableRescaleNormal()
                    RenderSystem.enableAlphaTest()
                    RenderSystem.defaultAlphaFunc()
                    RenderSystem.enableBlend()
                    RenderSystem.blendFunc(
                        GlStateManager.SrcFactor.SRC_ALPHA,
                        GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA
                    )
                    RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f)

                    val item = mc.itemRenderer ?: return@postDraw
                    val player = mc.player ?: return@postDraw

                    // render inventory
                    (1..4).forEach { row ->
                        (0 until 9).forEach { column ->
                            val slot = row * 9 + column
                            val stack = player.inventory.getStack(slot)
                            val isEmpty = stack.isEmpty
                            if (!isEmpty) {
                                val x = (rect.min.x.toInt() / scale) + column * 16 + 2
                                val y = (rect.min.y.toInt() / scale) + (row - 1) * 16 + 2
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

                    RenderSystem.disableAlphaTest()
                    RenderSystem.disableRescaleNormal()
                }
            })
        }
    }
}
