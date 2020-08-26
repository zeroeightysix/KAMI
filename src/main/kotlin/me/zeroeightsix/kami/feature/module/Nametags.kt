package me.zeroeightsix.kami.feature.module

import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting
import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.RenderEvent
import me.zeroeightsix.kami.event.RenderGuiEvent
import me.zeroeightsix.kami.interpolatedPos
import me.zeroeightsix.kami.setting.GenerateType
import me.zeroeightsix.kami.util.Target
import me.zeroeightsix.kami.util.Targets
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

    @Setting
    var targets = Targets(
        mapOf(
            Target.LIVING to NametagsTarget()
        )
    )

    var renderQueue: List<Pair<Entity, Vec2f>>? = null

    @EventHandler
    val worldRenderListener = Listener<RenderEvent.World>({ event ->
        val scale = MinecraftClient.getInstance().window.calculateScaleFactor(
            MinecraftClient.getInstance().options.guiScale,
            MinecraftClient.getInstance().forcesUnicodeFont()
        ).toFloat()

        val camera = mc.gameRenderer?.camera!!
        val viewport = VectorMath.getViewport()
        renderQueue = targets.entities.mapNotNull { (entity, _) ->
            val interpolated = entity.interpolatedPos
            VectorMath.divideVec2f(
                VectorMath.project3Dto2D(
                    camera.pos.negate()
                        .add(interpolated.add(0.0, entity.getEyeHeight(entity.pose).toDouble() + 1, 0.0)),
                    viewport,
                    event.matrixStack.peek().model,
                    event.projection
                ), scale
            )?.let { p ->
                entity to Vec2f(p.x, mc.window.scaledHeight - p.y)
            }
        }
    })

    // If this listener is invoked before KamiHud's listener (shouldn't happen due to priority),
    // it causes https://github.com/kotlin-graphics/imgui/issues/114
    // Looks like rendering text at specific times, just before imgui decides to initialise everything,
    // causes its textures to screw up.
    // weird.
    @EventHandler
    val hudRenderListener = Listener<RenderGuiEvent>({
        renderQueue?.forEach { (entity, pos) ->
            val text = entity.displayName.string
            val width = mc.textRenderer.getWidth(text)
            mc.textRenderer.draw(it.matrixStack, text, pos.x - width / 2, pos.y, 0xFFFFFF)
            renderQueue = null
        }
    })

    @GenerateType("Options")
    class NametagsTarget(var health: Boolean = true, var items: Items = Items.JUST_ITEMS) {
        enum class Items {
            JUST_ITEMS, ITEMS_AND_ENCHANTS
        }
    }

}
