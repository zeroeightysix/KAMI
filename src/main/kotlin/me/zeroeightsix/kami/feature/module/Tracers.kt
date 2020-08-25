package me.zeroeightsix.kami.feature.module

import com.mojang.blaze3d.platform.GlStateManager.*
import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting
import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.Colour
import me.zeroeightsix.kami.event.RenderEvent
import me.zeroeightsix.kami.interpolatedPos
import me.zeroeightsix.kami.noBobbingCamera
import me.zeroeightsix.kami.util.Target
import me.zeroeightsix.kami.util.Targets
import net.minecraft.client.render.Camera
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexFormats
import net.minecraft.util.math.Vec3d
import org.lwjgl.opengl.GL11

@Module.Info(
    name = "Tracers",
    description = "Draws lines to other living entities",
    category = Module.Category.RENDER
)
object Tracers : Module() {

    @Setting
    private var targets = Targets(
        mapOf(
            Target.ALL_PLAYERS to Colour.WHITE
        )
    )

    @Setting
    private var range = 200.0

    @EventHandler
    val worldListener = Listener<RenderEvent.World>({
        val player = mc.player ?: return@Listener

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
                .rotateX(-Math.toRadians(camera.pitch.toDouble()).toFloat())
                .rotateY(-Math.toRadians(camera.yaw.toDouble()).toFloat())

            bufferBuilder.begin(GL11.GL_LINES, VertexFormats.POSITION_COLOR)

            targets.entities
                .filter { (entity, _) -> player.distanceTo(entity) < range }
                .forEach { (entity, c) ->
                    val pos = entity.interpolatedPos

                    bufferBuilder.vertex(eyes.x, eyes.y, eyes.z)
                        .color(c.r, c.g, c.b, c.a).next()
                    bufferBuilder.vertex(pos.x - cX, pos.y - cY, pos.z - cZ)
                        .color(c.r, c.g, c.b, c.a).next()
                    bufferBuilder.vertex(pos.x - cX, pos.y - cY, pos.z - cZ)
                        .color(c.r, c.g, c.b, c.a).next()
                    bufferBuilder.vertex(pos.x - cX, pos.y - cY + entity.getEyeHeight(entity.pose), pos.z - cZ)
                        .color(c.r, c.g, c.b, c.a).next()
                }

            tessellator.draw()

        }

        enableTexture()
        enableDepthTest()
        lineWidth(1.0f)
    })
}
