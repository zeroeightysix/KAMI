package me.zeroeightsix.kami.gui.widgets

import com.mojang.blaze3d.systems.RenderSystem
import glm_.vec2.Vec2
import imgui.ImGui
import me.zeroeightsix.kami.gui.KamiGuiScreen
import me.zeroeightsix.kami.gui.KamiHud
import me.zeroeightsix.kami.matrix
import me.zeroeightsix.kami.mc
import me.zeroeightsix.kami.setting.GenerateType
import me.zeroeightsix.kami.to
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.LivingEntity

@GenerateType
class PlayerPinnableWidget(
    name: String,
    position: Position = Position.TOP_LEFT,
    open: Boolean = true,
    var size: Float = 30f,
) : PinnableWidget(name, position, open) {
    var tempSize = size
    @Transient
    private var height = 0f

    override fun preWindow() {
        val scale = KamiHud.getScale()
        height = size * (mc.player?.height
            ?: 1).toFloat() * scale * 1.1f // Extra margin because the player doesn't always fit inside
        ImGui.setNextWindowSize(Vec2(size * scale, height))
    }

    override fun fillWindow() {
        val guiOpen = mc.currentScreen is KamiGuiScreen

        if (!guiOpen) {
            val rect = ImGui.currentWindow.rect()
            ImGui.currentWindow.drawList.addCallback({ _, cmd ->
                KamiHud.postDraw {
                    val player = mc.player ?: return@postDraw

                    val scale = KamiHud.getScale()
                    it.matrix {
                        it.translate(
                            (rect.min.x.toDouble() + rect.width * 0.5f) / scale,
                            rect.max.y.toDouble() / scale,
                            500.0
                        )
                        it.scale(size, -size, -size)
                        val entityRenderDispatcher = MinecraftClient.getInstance().entityRenderDispatcher
                        val immediate = MinecraftClient.getInstance().bufferBuilders.entityVertexConsumers

                        entityRenderDispatcher.setRenderShadows(false)
                        RenderSystem.runAsFancy {
                            entityRenderDispatcher.render<LivingEntity>(
                                mc.player,
                                0.0,
                                player.isFallFlying.to(player.height.toDouble() / 2.0, 0.0),
                                0.0,
                                0f,
                                mc.tickDelta,
                                it,
                                immediate,
                                0xF000F0
                            )
                        }
                        entityRenderDispatcher.setRenderShadows(true)
                        immediate.draw()
                    }
                }
            })
        } else {
            ImGui.setNextItemWidth(-1f)
            ImGui.vSliderFloat(
                "Size",
                Vec2(24, height - ImGui.style.windowPadding.y * 2),
                vMin = 10f,
                vMax = 60f,
                v = ::tempSize,
                format = "%.0f"
            )
            if (!ImGui.isItemActive && tempSize != size) {
                println("fuk shit")
                size = tempSize
            }
        }
    }
}
