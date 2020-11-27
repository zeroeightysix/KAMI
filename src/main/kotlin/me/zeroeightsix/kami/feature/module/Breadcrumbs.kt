@file:Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")

package me.zeroeightsix.kami.feature.module

import com.mojang.blaze3d.systems.RenderSystem
import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Listener
import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting
import me.zero.alpine.listener.EventHandler
import me.zeroeightsix.kami.Colour
import me.zeroeightsix.kami.event.RenderEvent
import me.zeroeightsix.kami.event.TickEvent
import me.zeroeightsix.kami.matrix
import me.zeroeightsix.kami.setting.SettingVisibility
import me.zeroeightsix.kami.vertex
import net.minecraft.client.gl.VertexBuffer
import net.minecraft.client.render.BufferBuilder
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexFormats
import net.minecraft.util.math.Vec3d
import org.lwjgl.opengl.GL11
import me.zero.alpine.listener.Listener as AlpineListener
import java.lang.Boolean as JavaBoolean

@Suppress("DEPRECATION")
@Module.Info(
    name = "Breadcrumbs",
    description = "Show a trail of where the player has been",
    category = Module.Category.RENDER
)
object Breadcrumbs : Module() {

    @Setting(comment = "Clear the trail when the module is disabled")
    var clearOnDisable = true

    @Setting(comment = "Track trail even when the module is disabled")
    @SettingVisibility.Method("showTrackDisabled")
    var trackWhenDisabled = false

    @Setting(comment = "Draws the breadcrumbs line dashed")
    var dashedLine = true

    @Setting
    var colour = Colour(1f, 0.86f, 0.2f, 0.2f)

    private val drawMode: Int
        get() = if (dashedLine) GL11.GL_LINES else GL11.GL_LINE_STRIP

    @Suppress("unused")
    fun showTrackDisabled() = !clearOnDisable

    private const val BUFFER_SIZE = 300

    private val positions = mutableListOf<Vec3d>()
    private val buffers = mutableListOf<VertexBuffer>()

    @Listener("TrackWhenDisabled")
    fun onTrackWhenDisabledChanged(old: JavaBoolean?, new: JavaBoolean?) {
        alwaysListening = !trackWhenDisabled
    }

    override fun onDisable() {
        if (clearOnDisable) {
            buffers.forEach {
                it.close()
            }
            buffers.clear()

            nextPositions()
        }
    }

    private fun nextPositions() {
        // We preserve the last vertex in the positions list to connect the previous buffer.
        val last = positions.lastOrNull()
        positions.clear()
        last?.let {
            positions.add(it)
        }
    }

    @EventHandler
    val tickListener = AlpineListener({ event: TickEvent.InGame ->
        val pos = event.player.pos
        if (positions.lastOrNull()?.squaredDistanceTo(pos)?.let { it < 0.1 * 0.1 } == true) return@AlpineListener
        positions.add(pos)
    })

    @EventHandler
    val renderListener = AlpineListener({ event: RenderEvent.World ->
        val tessellator = Tessellator.getInstance()
        val builder: BufferBuilder = tessellator.buffer

        RenderSystem.color4f(colour.r, colour.g, colour.b, colour.a)

        event.matrixStack.matrix {
            val camera = mc.gameRenderer.camera.pos
            event.matrixStack.translate(-camera.x, -camera.y, -camera.z)

            if (positions.size >= BUFFER_SIZE) {
                val buffer = VertexBuffer(VertexFormats.POSITION)
                drawLines(builder)
                builder.end()
                buffer.upload(builder)

                buffers.add(buffer)

                nextPositions()
            } else {
                // deprecated push/pop because tesselator/bufferbuilder don't support translations, so we get the camera translation from this
                // not necessary for the VBO drawing as it does support taking the model (but currently does the same as below)
                RenderSystem.pushMatrix()
                RenderSystem.loadIdentity()
                RenderSystem.multMatrix(event.matrixStack.peek().model)

                drawLines(builder)
                tessellator.draw()

                RenderSystem.popMatrix()
            }

            buffers.forEach { buffer ->
                buffer.bind()
                VertexFormats.POSITION.startDrawing(0L)
                buffer.draw(event.matrixStack.peek().model, drawMode)
                VertexBuffer.unbind()
                VertexFormats.POSITION.endDrawing()
            }
        }
    })

    private fun drawLines(bufferBuilder: BufferBuilder) {
        bufferBuilder.begin(drawMode, VertexFormats.POSITION)
        positions.forEach {
            bufferBuilder.vertex(it).next()
        }
    }
}