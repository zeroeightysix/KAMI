package me.zeroeightsix.kami.gui.widgets

import com.mojang.blaze3d.systems.RenderSystem
import glm_.vec2.Vec2
import imgui.ImGui
import me.zeroeightsix.kami.gui.KamiGuiScreen
import me.zeroeightsix.kami.gui.KamiHud
import me.zeroeightsix.kami.matrix
import me.zeroeightsix.kami.mc
import me.zeroeightsix.kami.setting.GenerateType
import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.client.util.math.Vector3f
import net.minecraft.entity.LivingEntity

@GenerateType
class PlayerPinnableWidget(
    name: String,
    position: Position = Position.TOP_LEFT,
    open: Boolean = true,
    var size: Float = 30f,
    pinned: Boolean = true,
    background: Boolean = false
) : PinnableWidget(name, position, open, pinned, background) {
    var tempSize = size

    @Transient
    private var height = 0f

    override fun preWindow() {
        val scale = KamiHud.getScale()
        height = size * (
            mc.player?.height
                ?: 1
            ).toFloat() * scale * 1.1f // Extra margin because the player doesn't always fit inside
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
                    this.drawEntity(
                        (rect.min.x.toDouble() + rect.width * 0.5f) / scale,
                        rect.max.y.toDouble() / scale,
                        if (player.isFallFlying) {
                            player.height.toDouble() / 2.0
                        } else 0.0,
                        this.size,
                        player,
                        mc.tickDelta
                    )
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
                size = tempSize
            }
        }
    }

    fun drawEntity(x: Double, y: Double, offsetY: Double = 0.0, scale: Float, livingEntity: LivingEntity, tickDelta: Float) {
        RenderSystem.pushMatrix()
        RenderSystem.translatef(x.toFloat(), y.toFloat(), 1050.0f)
        RenderSystem.scalef(1.0f, 1.0f, -1.0f)
        val matrixStack = MatrixStack()
        matrixStack.translate(0.0, 0.0, 1000.0)
        matrixStack.scale(scale, scale, scale)
        val quaternion = Vector3f.POSITIVE_Z.getDegreesQuaternion(180.0f)
        matrixStack.multiply(quaternion)
        val entityRenderDispatcher = MinecraftClient.getInstance().entityRenderDispatcher
        entityRenderDispatcher.setRenderShadows(false)
        val immediate = MinecraftClient.getInstance().bufferBuilders.entityVertexConsumers
        RenderSystem.runAsFancy {
            entityRenderDispatcher.render(
                livingEntity,
                0.0,
                offsetY,
                0.0,
                0.0f,
                tickDelta,
                matrixStack,
                immediate,
                15728880
            )
        }
        immediate.draw()
        entityRenderDispatcher.setRenderShadows(true)
        RenderSystem.popMatrix()
    }
}
