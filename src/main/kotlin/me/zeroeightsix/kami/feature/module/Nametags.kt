package me.zeroeightsix.kami.feature.module

import com.mojang.blaze3d.platform.GlStateManager
import com.mojang.blaze3d.systems.RenderSystem
import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting
import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.Colour
import me.zeroeightsix.kami.event.RenderEvent
import me.zeroeightsix.kami.event.RenderGuiEvent
import me.zeroeightsix.kami.feature.module.Module.Category
import me.zeroeightsix.kami.feature.module.Module.Info
import me.zeroeightsix.kami.getInterpolatedPos
import me.zeroeightsix.kami.matrix
import me.zeroeightsix.kami.setting.GenerateType
import me.zeroeightsix.kami.util.Target
import me.zeroeightsix.kami.util.Targets
import me.zeroeightsix.kami.util.VectorMath
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.*
import net.minecraft.client.render.OverlayTexture.DEFAULT_UV
import net.minecraft.client.render.model.json.ModelTransformation
import net.minecraft.client.texture.SpriteAtlasTexture
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.client.util.math.Vector4f
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.util.math.Matrix4f
import kotlin.math.roundToInt

@Info(
    name = "Nametags",
    description = "Draws detailed nametags above entities",
    category = Category.RENDER
)
object Nametags : Module() {

    @Setting
    var targets = Targets(
        mapOf(
            Target.PASSIVE to NametagsTarget(false, colour = Colour(0.3f, 0.3f, 1f, 0.3f)),
            Target.HOSTILE to NametagsTarget(colour = Colour(0.75f, 1f, 0.3f, 0.3f)),
            Target.ALL_PLAYERS to NametagsTarget(
                distance = true,
                items = NametagsTarget.Items.JUST_ITEMS,
                colour = Colour(1f, 1f, 1f, 1f)
            )
        )
    )

    var renderQueue: List<Triple<Entity, Vector4f, NametagsTarget>>? = null

    @EventHandler
    val worldRenderListener = Listener<RenderEvent.World>({ event ->
        val scale = MinecraftClient.getInstance().window.calculateScaleFactor(
            MinecraftClient.getInstance().options.guiScale,
            MinecraftClient.getInstance().forcesUnicodeFont()
        ).toFloat()

        val camera = mc.gameRenderer?.camera!!
        val model = event.matrixStack.peek().model
        val cameraNegated = camera.pos.negate()
        renderQueue = targets.entities.mapNotNull { (entity, properties) ->
            val interpolated = entity.getInterpolatedPos(event.tickDelta)
            VectorMath.project3Dto2D(
                cameraNegated.add(interpolated.add(0.0, entity.getEyeHeight(entity.pose).toDouble() + 1, 0.0)),
                model,
                event.projection
            )?.let { p ->
                Triple(entity, Vector4f(p.x / scale, (mc.window.scaledHeight - p.y / scale), p.z, p.w), properties)
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
            val colour = properties.colour.asARGB()
            mc.textRenderer.drawWithShadow(it.matrixStack, text, pos.x - width / 2, pos.y, colour)
            if (properties.health && entity is LivingEntity) {
                drawHealthBar(bufferBuilder, width, pos, matrix, fHWidth, entity, it.matrixStack, properties.colour)
            }
            if (properties.distance) {
                it.matrixStack.matrix {
                    val distText = "${pos.w.roundToInt()}m"
                    val distW = mc.textRenderer.getWidth(distText) / 4f
                    it.matrixStack.translate(
                        pos.x.toDouble() - distW,
                        pos.y.toDouble() + mc.textRenderer.fontHeight,
                        0.0
                    )
                    if (properties.health) it.matrixStack.translate(0.0, 3.0, 0.0) // 3 ~= health bar height
                    it.matrixStack.scale(0.5f, 0.5f, 0f)
                    mc.textRenderer.drawWithShadow(it.matrixStack, distText, 0f, 0f, properties.colour.asARGB())
                }
            }
            properties.items.renderer(it, pos, entity)
            renderQueue = null
        }
    })

    private fun drawHealthBar(
        bufferBuilder: BufferBuilder,
        width: Int,
        pos: Vector4f,
        matrix: Matrix4f?,
        fHWidth: Float,
        entity: LivingEntity,
        matrices: MatrixStack,
        colour: Colour
    ) {
        with(bufferBuilder) {
            val xOffset = -width / 2
            val x = pos.x + xOffset
            val y = pos.y + mc.textRenderer.fontHeight
            val z = pos.w
            fun fill(r: Float, g: Float, b: Float, a: Float, width: Float, height: Float = 2f) {
                vertex(matrix, x, y + height, z).color(r, g, b, a).next()
                vertex(matrix, x + width, y + height, z).color(r, g, b, a).next()
                vertex(matrix, x + width, y, z).color(r, g, b, a).next()
                vertex(matrix, x, y, z).color(r, g, b, a).next()
            }

            RenderSystem.enableBlend()
            RenderSystem.disableTexture()
            RenderSystem.defaultBlendFunc()

            // We never want the bar width to be extremely small, so we limit it to being as big as the health text.
            val barWidth = (width.toFloat() - fHWidth).coerceAtLeast(fHWidth) - 1

            begin(7, VertexFormats.POSITION_COLOR)
            fill(0f, 0f, 0f, colour.a * 0.5f, barWidth, 2.5f)
            fill(1f, 0f, 0f, colour.a * 0.7f, barWidth * (entity.health / entity.maxHealth))
            end()

            BufferRenderer.draw(this)
            RenderSystem.enableTexture()
            RenderSystem.disableBlend()

            // push & pop to preserve scale
            matrices.matrix {
                matrices.translate((x + barWidth).toDouble() + 1, y.toDouble(), 0.0)
                matrices.scale(.5f, .5f, 1f)

                // To align the center baseline with the health bar
                // Divisions: center and scale
                val textY = -mc.textRenderer.fontHeight / 2f / 2f
                mc.textRenderer.drawWithShadow(matrices, "${entity.health.roundToInt()}", 0f, textY, colour.asARGB())
            }
        }
    }

    @GenerateType("Options")
    class NametagsTarget(
        var health: Boolean = true,
        var distance: Boolean = false,
        var items: Items = Items.NONE,
        var colour: Colour = Colour.WHITE
    ) {
        enum class Items(val renderer: (RenderGuiEvent, Vector4f, Entity) -> Unit) {
            NONE({ _, _, _ -> Unit }),
            JUST_ITEMS({ event, pos, entity ->
                val item = mc.itemRenderer
                val equipped = entity.itemsEquipped.filter { !it.isEmpty }

                val immediate = mc.bufferBuilders.entityVertexConsumers

                mc.textureManager.bindTexture(SpriteAtlasTexture.BLOCK_ATLAS_TEX)
                mc.textureManager.getTexture(SpriteAtlasTexture.BLOCK_ATLAS_TEX)!!.setFilter(false, false)

                RenderSystem.enableRescaleNormal()
                RenderSystem.enableAlphaTest()
                RenderSystem.defaultAlphaFunc()
                RenderSystem.enableBlend()
                RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA)
                RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f)

                event.matrixStack.matrix {
                    event.matrixStack.translate(pos.x + 8.0, (pos.y - mc.textRenderer.fontHeight).toDouble(), 150.0)
                    event.matrixStack.scale(1.0F, -1.0F, 1.0F)
                    event.matrixStack.scale(16.0F, 16.0F, 16.0F)
                    event.matrixStack.translate(-(equipped.size) / 2.0, 0.0, 0.0)

                    equipped.forEach {
                        val model = item.getHeldItemModel(it, null, null)

                        val bl: Boolean = !model.isSideLit
                        if (bl) {
                            DiffuseLighting.disableGuiDepthLighting()
                        }

                        item.renderItem(
                            it,
                            ModelTransformation.Mode.GUI,
                            false,
                            event.matrixStack,
                            immediate,
                            0xF000F0,
                            DEFAULT_UV,
                            model
                        )
                        immediate.draw()

                        if (bl) {
                            DiffuseLighting.enableGuiDepthLighting()
                        }

                        event.matrixStack.translate(1.0, 0.0, 0.0)
                    }
                }

                RenderSystem.disableAlphaTest()
                RenderSystem.disableRescaleNormal()
            }),
            ITEMS_AND_ENCHANTS({ event, pos, entity ->
                JUST_ITEMS.renderer(event, pos, entity)
            })
        }
    }

}
