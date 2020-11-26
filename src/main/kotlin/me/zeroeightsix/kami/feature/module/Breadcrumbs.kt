@file:Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")

package me.zeroeightsix.kami.feature.module

import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Listener
import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting
import me.zero.alpine.listener.EventHandler
import me.zeroeightsix.kami.Colour
import me.zeroeightsix.kami.colour
import me.zeroeightsix.kami.event.RenderEvent
import me.zeroeightsix.kami.event.TickEvent
import me.zeroeightsix.kami.setting.SettingVisibility
import me.zeroeightsix.kami.util.minus
import me.zeroeightsix.kami.vertex
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexFormats
import net.minecraft.util.math.Vec3d
import org.lwjgl.opengl.GL11
import me.zero.alpine.listener.Listener as AlpineListener
import java.lang.Boolean as JavaBoolean

@Module.Info(name = "Breadcrumbs", description = "Show a trail of where the player has been", category = Module.Category.RENDER)
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

    private const val BUFFER_SIZE = 100

    private val positions = mutableListOf<Vec3d>()

    @Listener("TrackWhenDisabled")
    fun onTrackWhenDisabledChanged(old: JavaBoolean?, new: JavaBoolean?) {
        alwaysListening = !trackWhenDisabled
    }

    override fun onDisable() {
        if (clearOnDisable)
            positions.clear()
    }

    @EventHandler
    val tickListener = AlpineListener({ event: TickEvent.InGame ->
        val pos = event.player.pos
        if (positions.lastOrNull()?.squaredDistanceTo(pos)?.let { it < 0.1 * 0.1 } == true) return@AlpineListener
        positions.add(pos)
    })

    @EventHandler
    val renderListener = AlpineListener({ _: RenderEvent.World ->
        val camera = mc.gameRenderer.camera.pos
        val tessellator = Tessellator.getInstance()
        val bufferBuilder = tessellator.buffer

        bufferBuilder.begin(drawMode, VertexFormats.POSITION_COLOR)
        positions.forEach {
            bufferBuilder.vertex(it.minus(camera)).colour(this.colour).next()
        }
        tessellator.draw()
    })

}