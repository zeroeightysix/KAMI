package me.zeroeightsix.kami.feature.module

import com.mojang.blaze3d.platform.GlStateManager.*
import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting
import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.RenderEvent
import me.zeroeightsix.kami.event.TickEvent
import me.zeroeightsix.kami.noBobbingCamera
import me.zeroeightsix.kami.util.ColourUtils
import me.zeroeightsix.kami.util.EntityUtil
import me.zeroeightsix.kami.util.Friends
import me.zeroeightsix.kami.util.HueCycler
import net.minecraft.client.render.Camera
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexFormats
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.Vec3d
import org.lwjgl.opengl.GL11

/**
 * Created by 086 on 11/12/2017.
 */
@Module.Info(
    name = "Tracers",
    description = "Draws lines to other living entities",
    category = Module.Category.RENDER
)
object Tracers : Module() {

    @Setting
    private var players = true

    @Setting
    private var friends = true

    @Setting
    private var animals = false

    @Setting
    private var mobs = false

    @Setting
    private var range = 200.0

    @Setting
    private var opacity: @Setting.Constrain.Range(min = 0.0, max = 1.0, step = 0.1) Float = 0.5f
    var cycler = HueCycler(3600)

    @EventHandler
    val worldListener = Listener(
        EventHook<RenderEvent.World> {
            val camera: Camera = mc.gameRenderer.camera
            val tessellator = Tessellator.getInstance()
            val bufferBuilder = tessellator.buffer
            val cX = camera.pos.x
            val cY = camera.pos.y
            val cZ = camera.pos.z

            lineWidth(0.5f)
            disableTexture()
            disableDepthTest()

            noBobbingCamera(it.matrixStack) {
                val eyes: Vec3d = Vec3d(0.0, 0.0, 0.1)
                    .rotateX(
                        (-mc.player?.pitch?.toDouble()?.let { it1 ->
                            Math
                                    .toRadians(it1)
                        }!!).toFloat()
                    )
                    .rotateY(
                        (-mc.player?.yaw?.toDouble()?.let { it1 ->
                            Math
                                    .toRadians(it1)
                        }!!).toFloat()
                    )

                bufferBuilder.begin(GL11.GL_LINES, VertexFormats.POSITION_COLOR)

                mc.world?.entities
                        ?.filter { EntityUtil.isLiving(it) && !EntityUtil.isFakeLocalPlayer(it) }
                        ?.filter {
                            when {
                                it is PlayerEntity -> players && mc.player !== it
                                EntityUtil.isPassive(
                                        it
                                ) -> animals
                                else -> mobs
                            }
                        }
                    ?.filter { mc.player?.distanceTo(it)!! < range }
                    ?.forEach {
                        var colour = getColour(it)
                        if (colour == ColourUtils.Colors.RAINBOW) {
                            if (!friends) return@forEach
                            colour = cycler.current()
                        }
                        val r = colour ushr 16 and 0xFF
                        val g = colour ushr 8 and 0xFF
                        val b = colour and 0xFF
                        val a = (opacity * 255f).toInt()

                        val pos = interpolate(it)

                        bufferBuilder.vertex(eyes.x, eyes.y, eyes.z)
                            .color(r, g, b, a).next()
                        bufferBuilder.vertex(pos.x - cX, pos.y - cY, pos.z - cZ)
                            .color(r, g, b, a).next()
                        bufferBuilder.vertex(pos.x - cX, pos.y - cY, pos.z - cZ)
                            .color(r, g, b, a).next()
                        bufferBuilder.vertex(pos.x - cX, pos.y - cY + it.getEyeHeight(it.pose), pos.z - cZ)
                            .color(r, g, b, a).next()
                    }

                tessellator.draw()

            }

            enableTexture()
            enableDepthTest()
            lineWidth(1.0f)
        }
    )

    @EventHandler
    val updateListener = Listener(
        EventHook<TickEvent.Client.InGame> {
            cycler.next()
        }
    )

    private fun getColour(entity: Entity): Int {
        return if (entity is PlayerEntity) {
            if (Friends.isFriend(entity.gameProfile.name)) ColourUtils.Colors.RAINBOW else ColourUtils.Colors.WHITE
        } else {
            if (EntityUtil.isPassive(entity)) ColourUtils.Colors.GREEN else ColourUtils.Colors.RED
        }
    }

    private fun interpolate(now: Double, then: Double): Double {
        return then + (now - then) * mc.tickDelta
    }

    private fun interpolate(entity: Entity): Vec3d {
        return Vec3d(
            interpolate(entity.x, entity.lastRenderX),
            interpolate(entity.y, entity.lastRenderY),
            interpolate(entity.z, entity.lastRenderZ)
        )
    }
}
