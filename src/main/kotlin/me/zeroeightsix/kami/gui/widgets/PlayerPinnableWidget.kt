package me.zeroeightsix.kami.gui.widgets

import com.mojang.blaze3d.systems.RenderSystem
import imgui.ImGui
import me.zeroeightsix.kami.gui.ImguiDSL.wrapSingleFloatArray
import me.zeroeightsix.kami.gui.KamiGuiScreen
import me.zeroeightsix.kami.gui.KamiHud
import me.zeroeightsix.kami.gui.KamiImgui
import me.zeroeightsix.kami.mc
import me.zeroeightsix.kami.setting.GenerateType
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
    private var tempSize = size

    @Transient
    private var height = 0f

    override fun preWindow() {
        val scale = KamiHud.getScale()
        height = size * (
            mc.player?.height
                ?: 1
            ).toFloat() * scale * 1.1f // Extra margin because the player doesn't always fit inside
        ImGui.setNextWindowSize(size * scale, height)
    }

    override fun fillWindow() {
        val guiOpen = mc.currentScreen is KamiGuiScreen

        if (!guiOpen) {
            val posX = ImGui.getWindowPosX() + (ImGui.getWindowWidth() * 0.5f)
            val posY = ImGui.getWindowPosY() + ImGui.getWindowHeight()
            KamiImgui.postDraw {
                val player = mc.player ?: return@postDraw
                val scale = KamiHud.getScale()
                this.drawEntity(
                    posX / scale,
                    posY / scale,
                    if (player.isFallFlying) {
                        player.height.toDouble() / 2.0
                    } else 0.0,
                    this.size,
                    player,
                    mc.tickDelta
                )
            }
        } else {
            ImGui.setNextItemWidth(-1f)
            wrapSingleFloatArray(::tempSize) {
                ImGui.vSliderFloat(
                    "Size",
                    24.0f, height - ImGui.getStyle().windowPaddingX * 2,
                    it,
                    10f,
                    60f,
                    "%.0f"
                )
            }
            if (!ImGui.isItemActive() && tempSize != size) {
                size = tempSize
            }
        }
    }

    private fun drawEntity(
        x: Float,
        y: Float,
        offsetY: Double = 0.0,
        scale: Float,
        livingEntity: LivingEntity,
        tickDelta: Float
    ) {
        RenderSystem.pushMatrix()
        RenderSystem.translatef(x, y, 1050.0f)
        RenderSystem.scalef(1.0f, 1.0f, -1.0f)
        val matrixStack = MatrixStack()
        matrixStack.translate(0.0, 0.0, 1000.0)
        matrixStack.scale(scale, scale, scale)
        val quaternion = Vector3f.POSITIVE_Z.getDegreesQuaternion(180.0f)
        matrixStack.multiply(quaternion)
        val entityRenderDispatcher = mc.entityRenderDispatcher
        entityRenderDispatcher.setRenderShadows(false)
        val immediate = mc.bufferBuilders.entityVertexConsumers
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
