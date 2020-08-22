package me.zeroeightsix.kami.feature.module

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.RenderEvent
import me.zeroeightsix.kami.event.RenderGuiEvent
import me.zeroeightsix.kami.interpolatedPos
import me.zeroeightsix.kami.util.VectorMath
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.Entity
import net.minecraft.util.math.Vec2f

@Module.Info(
    name = "Nametags",
    description = "Draws detailed nametags above entities",
    category = Module.Category.RENDER
)
object Nametags : Module() {

    var renderQueue: List<Pair<Entity, Vec2f>>? = null

    @EventHandler
    val worldRenderListener = Listener<RenderEvent.World>(EventHook { event ->
        val scale = MinecraftClient.getInstance().window.calculateScaleFactor(
            MinecraftClient.getInstance().options.guiScale,
            MinecraftClient.getInstance().forcesUnicodeFont()
        ).toFloat()

        val camera = mc.gameRenderer?.camera!!
        val viewport = VectorMath.getViewport()
        renderQueue = mc.world?.entities?.mapNotNull {
            val interpolated = it.interpolatedPos
            val p = VectorMath.divideVec2f(
                VectorMath.project3Dto2D(
                    camera.pos.negate().add(interpolated.add(0.0, it.getEyeHeight(it.pose).toDouble() + 1, 0.0)),
                    viewport,
                    event.matrixStack.peek().model,
                    event.projection
                ), scale
            )?.let {
                Vec2f(it.x, mc.window.scaledHeight - it.y)
            }
            p?.let { p ->
                it to p
            }
        }
    })

    @EventHandler
    val hudRenderListener = Listener<RenderGuiEvent>(EventHook {
        renderQueue?.forEach { (entity, pos) ->
            val text = entity.displayName.string
            val width = mc.textRenderer.getWidth(text)
            mc.textRenderer.draw(it.matrixStack, text, pos.x - width / 2, pos.y, 0xFFFFFF)
            renderQueue = null
        }
    })

}
