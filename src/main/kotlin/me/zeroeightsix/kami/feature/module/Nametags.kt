package me.zeroeightsix.kami.feature.module

import com.mojang.blaze3d.systems.RenderSystem
import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting
import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.Colour
import me.zeroeightsix.kami.event.RenderEvent
import me.zeroeightsix.kami.event.RenderGuiEvent
import me.zeroeightsix.kami.interpolatedPos
import me.zeroeightsix.kami.matrix
import me.zeroeightsix.kami.setting.GenerateType
import me.zeroeightsix.kami.util.Target
import me.zeroeightsix.kami.util.Targets
import me.zeroeightsix.kami.util.VectorMath
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.BufferBuilder
import net.minecraft.client.render.BufferRenderer
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexFormats
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.util.math.Matrix4f
import net.minecraft.util.math.Vec2f
import kotlin.math.roundToInt

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

    var renderQueue: List<Triple<Entity, Vec2f, NametagsTarget>>? = null

    @EventHandler
    val worldRenderListener = Listener<RenderEvent.World>({ event ->
        val scale = MinecraftClient.getInstance().window.calculateScaleFactor(
            MinecraftClient.getInstance().options.guiScale,
            MinecraftClient.getInstance().forcesUnicodeFont()
        ).toFloat()

        val camera = mc.gameRenderer?.camera!!
        val viewport = VectorMath.getViewport()
        renderQueue = targets.entities.mapNotNull { (entity, properties) ->
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
                Triple(entity, Vec2f(p.x, mc.window.scaledHeight - p.y), properties)
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
        // The 'reference width' for the health indicator
        // This is the width subtracted from the health bar's width instead of the actual health being rendered, for consistency
        // It is scaled down, so we divide by 2.
        val fHWidth = mc.textRenderer.getWidth("36") / 2f

        val bufferBuilder = Tessellator.getInstance().buffer
        val matrix = it.matrixStack.peek().model
        renderQueue?.forEach { (entity, pos, properties) ->
            val text = entity.displayName.string
            val width = mc.textRenderer.getWidth(text)
            mc.textRenderer.drawWithShadow(it.matrixStack, text, pos.x - width / 2, pos.y, 0xFFFFFF)
            if (properties.health && entity is LivingEntity) {
                drawHealthBar(bufferBuilder, width, pos, matrix, fHWidth, entity, it)
            }
            renderQueue = null
        }
    })

    private fun drawHealthBar(
        bufferBuilder: BufferBuilder,
        width: Int,
        pos: Vec2f,
        matrix: Matrix4f?,
        fHWidth: Float,
        entity: LivingEntity,
        it: RenderGuiEvent
    ) {
        with(bufferBuilder) {
            val xOffset = -width / 2
            val x = pos.x + xOffset
            val y = pos.y + mc.textRenderer.fontHeight
            fun fill(r: Float, g: Float, b: Float, a: Float, width: Float, height: Float = 2f) {
                vertex(matrix, x, y + height, 0f).color(r, g, b, a).next()
                vertex(matrix, x + width, y + height, 0f).color(r, g, b, a).next()
                vertex(matrix, x + width, y, 0f).color(r, g, b, a).next()
                vertex(matrix, x, y, 0f).color(r, g, b, a).next()
            }

            RenderSystem.enableBlend()
            RenderSystem.disableTexture()
            RenderSystem.defaultBlendFunc()

            // We never want the bar width to be extremely small, so we limit it to being as big as the health text.
            val barWidth = (width.toFloat() - fHWidth).coerceAtLeast(fHWidth) - 1

            begin(7, VertexFormats.POSITION_COLOR)
            fill(0f, 0f, 0f, 0.5f, barWidth, 2.5f)
            fill(1f, 0f, 0f, 0.7f, barWidth * (entity.health / entity.maxHealth))
            end()

            BufferRenderer.draw(this)
            RenderSystem.enableTexture()
            RenderSystem.disableBlend()

            // push & pop to preserve scale
            it.matrixStack.matrix {
                it.matrixStack.translate((x + barWidth).toDouble() + 1, y.toDouble(), 0.0)
                it.matrixStack.scale(.5f, .5f, 1f)

                // To align the center baseline with the health bar
                // Divisions: center and scale
                val textY = -mc.textRenderer.fontHeight / 2f / 2f
                mc.textRenderer.drawWithShadow(it.matrixStack, "${entity.health.roundToInt()}", 0f, textY, 0xFFFFFF)
            }
        }
    }

    @GenerateType("Options")
    class NametagsTarget(var health: Boolean = true, var items: Items = Items.JUST_ITEMS) {
        enum class Items {
            JUST_ITEMS, ITEMS_AND_ENCHANTS
        }
    }

}
