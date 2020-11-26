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
import net.minecraft.client.render.Camera
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexFormats
import net.minecraft.util.math.Vec3d
import org.lwjgl.opengl.GL11
import me.zero.alpine.listener.Listener as AlpineListener

@Module.Info(name = "Breadcrumbs", description = "Show a trail of where the player has been", category = Module.Category.RENDER)
object Breadcrumbs : Module() {

    @Setting(comment = "Clear the trail when the module is disabled")
    val clearOnDisable = true

    @Setting(comment = "Track trail even when the module is disabled")
    @SettingVisibility.Method("showTrackDisabled")
    val trackWhenDisabled = false

    @Setting
    val colour = Colour(1f, 0.86f, 0.2f, 0.2f)

    @Suppress("unused")
    fun showTrackDisabled() = !clearOnDisable

    private const val BUFFER_SIZE = 100

    private val positions = mutableListOf<Vec3d>()

    @Listener("TrackWhenDisabled")
    fun onTrackWhenDisabledChanged() {
        alwaysListening = trackWhenDisabled
    }

    override fun onDisable() {
        if (clearOnDisable)
            positions.clear()
    }

    @EventHandler
    val tickListener = AlpineListener({ event: TickEvent.InGame ->
        val player = event.player
        positions.add(player.pos)
    })

    @EventHandler
    val renderListener = AlpineListener({ event: RenderEvent.World ->
        val camera = mc.gameRenderer.camera.pos
        val tessellator = Tessellator.getInstance()
        val bufferBuilder = tessellator.buffer

        bufferBuilder.begin(GL11.GL_LINES, VertexFormats.POSITION_COLOR)
        positions.forEach {
            bufferBuilder.vertex(it.minus(camera)).colour(this.colour).next()
        }
        tessellator.draw()
    })

}