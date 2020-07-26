package me.zeroeightsix.kami.feature.module

import com.mojang.blaze3d.platform.GlStateManager
import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.events.RenderEvent
import me.zeroeightsix.kami.mimic.ProjectileMimic
import me.zeroeightsix.kami.mimic.ThrowableMimic
import me.zeroeightsix.kami.times
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexFormats
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.item.BowItem
import net.minecraft.item.ItemStack
import net.minecraft.item.SnowballItem
import net.minecraft.util.Hand
import net.minecraft.util.math.Vec3d
import org.lwjgl.opengl.GL11

/**
 * Created by 086 on 28/12/2017.
 */
@Module.Info(
    name = "Trajectories",
    category = Module.Category.RENDER
)
object Trajectories : Module() {
    private var positions = mutableListOf<Vec3d>()

    private fun LivingEntity.getHeldItem(): ItemStack? {
        return if (isUsingItem) {
            activeItem
        } else {
            getStackInHand(Hand.MAIN_HAND) ?: getStackInHand(Hand.OFF_HAND)
        }
    }

    /**
     * Modified version of [BowItem.getPullProgress]
     *
     * Takes a float instead of int so tickDelta can be used here for interpolation
     */
    fun getPullProgress(useTicks: Float): Float {
        var f = useTicks / 20.0f
        f = (f * f + f * 2.0f) / 3.0f
        if (f > 1.0f) {
            f = 1.0f
        }
        return f
    }

    @EventHandler
    var worldListener = Listener(EventHook<RenderEvent.World> {
        val camera = mc.gameRenderer.camera
        val cX = camera.pos.x
        val cY = camera.pos.y
        val cZ = camera.pos.z

        val tesselator = Tessellator.getInstance()
        val buffer = tesselator.bufferBuilder

        GlStateManager.enableDepthTest()
        GlStateManager.enableBlend()
        GlStateManager.lineWidth(0.5F)

        mc.world.entities
            .filterIsInstance<LivingEntity>()
            .forEach {
                val mimic = when ((it.getHeldItem() ?: return@forEach).item) {
                    is BowItem -> {
                        if (!it.isUsingItem) return@forEach
                        val progress = getPullProgress(it.activeItem.maxUseTime - it.itemUseTimeLeft + mc.tickDelta)

                        val mimic = ProjectileMimic(mc.world, it)
                        mimic.setProperties(
                            it,
                            it.pitch,
                            it.yaw,
                            3 * progress
                        )

                        mimic
                    }
                    is SnowballItem -> {
                        val mimic = ThrowableMimic(mc.world, it, EntityType.SNOWBALL)

                        mimic.setProperties(
                            it,
                            it.pitch,
                            it.yaw,
                            1.5f
                        )

                        mimic
                    }
                    else -> return@forEach
                }

                var offset = if (it == mc.player) {
                    Vec3d(-0.1, 0.075, 0.0)
                        .rotateX((-Math.toRadians(mc.player.pitch.toDouble())).toFloat())
                        .rotateY((-Math.toRadians(mc.player.yaw.toDouble())).toFloat())
                } else {
                    Vec3d(0.0, 0.0, 0.0)
                }

                buffer.begin(GL11.GL_LINE_STRIP, VertexFormats.POSITION_COLOR)
                while (!mimic.landed) {
                    buffer.vertex(mimic.x - cX + offset.x, mimic.y - cY + offset.y, mimic.z - cZ + offset.z)
                        .color(1f, 1f, 1f, 1f)
                        .next()
                    mimic.tick()
                    offset *= 0.8
                }
                tesselator.draw()
            }

    })
}
