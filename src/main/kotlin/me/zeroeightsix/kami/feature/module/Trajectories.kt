package me.zeroeightsix.kami.feature.module

import com.mojang.blaze3d.platform.GlStateManager.*
import com.mojang.blaze3d.systems.RenderSystem
import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.events.RenderEvent
import me.zeroeightsix.kami.matrix
import me.zeroeightsix.kami.mimic.ProjectileMimic
import me.zeroeightsix.kami.mimic.ThrowableMimic
import me.zeroeightsix.kami.noBobbingCamera
import me.zeroeightsix.kami.times
import me.zeroeightsix.kami.unreachable
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexFormats
import net.minecraft.client.render.debug.ChunkBorderDebugRenderer
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.item.*
import net.minecraft.util.Hand
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import org.lwjgl.opengl.GL11
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Created by 086 on 28/12/2017.
 */
@Module.Info(
    name = "Trajectories",
    category = Module.Category.RENDER
)
object Trajectories : Module() {

    private var circleList = -1

    private fun compileList() {
        val tessellator = Tessellator.getInstance()
        val buffer = tessellator.buffer

        with(buffer) {
            begin(GL11.GL_TRIANGLE_FAN, VertexFormats.POSITION_COLOR)

            vertex(0.0, 0.0, 0.0).color(1f, 1f, 1f, 0.4f).next()
            for (angle in 0..50) {
                val angle = (angle.toDouble() / 50.0) * 2 * PI
                vertex(sin(angle), cos(angle), 0.0).color(1f, 1f, 1f, 0.4f).next()
            }

            tessellator.draw()
        }
    }

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
    private fun getPullProgress(useTicks: Float): Float {
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

        val tessellator = Tessellator.getInstance()
        val buffer = tessellator.buffer

        RenderSystem.shadeModel(GL11.GL_SMOOTH)
        RenderSystem.enableAlphaTest()
        RenderSystem.defaultAlphaFunc()
        RenderSystem.disableTexture()
        lineWidth(0.5F)

        if (circleList == -1)
            compileList()

        noBobbingCamera(it.matrixStack) {
            mc.world?.entities
                ?.filterIsInstance<LivingEntity>()
                ?.forEach {
                    val stack = it.getHeldItem() ?: return@forEach
                    val mimic = when (stack.item) {
                        is BowItem, is TridentItem -> {
                            if (!it.isUsingItem) return@forEach

                            val isBow = stack.item is BowItem

                            val power = if (isBow) {
                                getPullProgress(stack.maxUseTime - it.itemUseTimeLeft + mc.tickDelta) * 3
                            } else {
                                2.5f + EnchantmentHelper.getRiptide(stack) * 0.5f
                            }

                            val mimic = ProjectileMimic(
                                mc?.world!!,
                                it,
                                if (isBow) {
                                    EntityType.ARROW
                                } else {
                                    EntityType.TRIDENT
                                },
                                if (isBow) {
                                    0.6
                                } else {
                                    0.99
                                },
                                1.0
                            )
                            mimic.setProperties(
                                it,
                                it.pitch,
                                it.yaw,
                                power
                            )

                            mimic
                        }
                        is SnowballItem, is EggItem, is EnderPearlItem, is ExperienceBottleItem -> {
                            val type = when (stack.item) {
                                is SnowballItem -> EntityType.SNOWBALL
                                is EggItem -> EntityType.EGG
                                is EnderPearlItem -> EntityType.ENDER_PEARL
                                is ExperienceBottleItem -> EntityType.EXPERIENCE_BOTTLE
                                else -> unreachable()
                            }

                            val (power, pitchOffset, gravity) = when (stack.item) {
                                is ExperienceBottleItem -> Triple(0.7f, -20f, 0.06)
                                else -> Triple(1.5f, 0f, 0.03)
                            }

                            val mimic = ThrowableMimic(mc.world!!, it, type, gravity, 1.0)

                            mimic.setProperties(
                                it.pitch,
                                it.yaw,
                                pitchOffset,
                                power
                            )

                            mimic
                        }
                        else -> return@forEach
                    }

                    var offset = if (it == mc.player) {
                        Vec3d(-0.1, 0.075, 0.0)
                            .rotateX((-Math.toRadians(mc.player!!.pitch.toDouble())).toFloat())
                            .rotateY((-Math.toRadians(mc.player!!.yaw.toDouble())).toFloat())
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
                    tessellator.draw()

                    mimic.hit?.let { hit ->
                        matrix {
                            translated(hit.x - cX, hit.y - cY, hit.z - cZ)
                            scaled(mimic.diverged, mimic.diverged, mimic.diverged)
                            mimic.face?.let {
                                rotatef(-it.asRotation(), 0.0f, 1.0f, 0.0f)
                                if (it == Direction.DOWN || it == Direction.UP) {
                                    rotatef(90f, 1f, 0f, 0f)
                                }
                            }

                            RenderSystem.disableCull()
                            RenderSystem.disableDepthTest()
                            RenderSystem.enableDepthTest()
                            RenderSystem.enableCull()
                        }
                    }
                }
        }

        RenderSystem.lineWidth(1.0f)
        RenderSystem.enableBlend()
        RenderSystem.enableTexture()
        RenderSystem.shadeModel(GL11.GL_FLAT)
    })
}
