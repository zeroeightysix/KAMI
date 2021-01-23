package me.zeroeightsix.kami.feature.module

import com.mojang.blaze3d.platform.GlStateManager
import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting
import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.Colour
import me.zeroeightsix.kami.event.RenderEvent
import me.zeroeightsix.kami.getInterpolatedPos
import me.zeroeightsix.kami.noBobbingCamera
import me.zeroeightsix.kami.target.EntityCategory
import me.zeroeightsix.kami.target.EntitySupplier
import net.minecraft.client.render.Camera
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexFormats
import net.minecraft.util.math.Vec3d
import org.lwjgl.opengl.GL11.*

@Module.Info(
    name = "Tracers",
    description = "Draws lines to other living entities",
    category = Module.Category.RENDER
)
object Tracers : Module() {

    @Setting
    private var targets = EntitySupplier(
        mapOf(
            EntityCategory.ALL_PLAYERS to Colour.WHITE
        ),
        mapOf()
    )

    @Setting
    private var range = 200.0

    @Setting
    private var thickness: @Setting.Constrain.Range(
    min = 0.1,
    max = 8.0,
    step = 0.1
    ) Float = 1.5f

    @EventHandler
    private val worldListener = Listener<RenderEvent.World>({ event ->
        val player = mc.player ?: return@Listener

        val camera: Camera = mc.gameRenderer.camera
        val tessellator = Tessellator.getInstance()
        val bufferBuilder = tessellator.buffer
        val cX = camera.pos.x
        val cY = camera.pos.y
        val cZ = camera.pos.z

        GlStateManager.lineWidth(thickness)
        GlStateManager.disableTexture()
        GlStateManager.disableDepthTest()

        glDisable(GL_LINE_SMOOTH)
        glHint(GL_LINE_SMOOTH_HINT, GL_NICEST)

        noBobbingCamera(event.matrixStack) {
            val eyes: Vec3d = Vec3d(0.0, 0.0, 0.1)
                .rotateX(-Math.toRadians(camera.pitch.toDouble()).toFloat())
                .rotateY(-Math.toRadians(camera.yaw.toDouble()).toFloat())

            bufferBuilder.begin(GL_LINES, VertexFormats.POSITION_COLOR)

            targets.targets
                .filter { (entity, _) -> player.distanceTo(entity) < range }
                .forEach { (entity, c) ->
                    val pos = entity.getInterpolatedPos(event.tickDelta)

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

        GlStateManager.lineWidth(1.0f)
        GlStateManager.enableTexture()
        GlStateManager.enableDepthTest()

        glDisable(GL_LINE_SMOOTH)
        glHint(GL_LINE_SMOOTH_HINT, GL_DONT_CARE)
    })
}
