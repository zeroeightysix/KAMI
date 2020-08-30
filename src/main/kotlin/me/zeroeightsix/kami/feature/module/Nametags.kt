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
import me.zeroeightsix.kami.util.EntityTarget
import me.zeroeightsix.kami.util.EntityTargets
import me.zeroeightsix.kami.util.VectorMath
import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.render.*
import net.minecraft.client.render.OverlayTexture.DEFAULT_UV
import net.minecraft.client.render.item.ItemRenderer
import net.minecraft.client.render.model.json.ModelTransformation
import net.minecraft.client.texture.SpriteAtlasTexture
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.client.util.math.Vector4f
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.minecraft.text.LiteralText
import net.minecraft.text.StringVisitable
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Matrix4f
import net.minecraft.util.registry.Registry
import org.lwjgl.opengl.GL11
import java.util.*
import kotlin.math.roundToInt

@Info(
    name = "Nametags",
    description = "Draws detailed nametags above entities",
    category = Category.RENDER
)
object Nametags : Module() {

    @Setting
    var targets = EntityTargets(
        mapOf(
            EntityTarget.PASSIVE to NametagsTarget(false, colour = Colour(0.3f, 0.3f, 1f, 0.3f)),
            EntityTarget.HOSTILE to NametagsTarget(colour = Colour(0.75f, 1f, 0.3f, 0.3f)),
            EntityTarget.ALL_PLAYERS to NametagsTarget(
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
                cameraNegated.add(interpolated.add(0.0, entity.getEyeHeight(entity.pose).toDouble() + 0.5, 0.0)),
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

            // When rendering text, the coordinate passed is the top-left of where the text will be rendered.
            // We want everything to be rendered 'above' the 2D position (consistent scaling), so we subtract the font height.
            var y = pos.y - mc.textRenderer.fontHeight

            // We draw bottom-to-top to easily shift the y coordinate based on what does get rendered and what not.
            // If we drew top-to-bottom, we'd first have to calculate the height of all elements before being able to render them,
            // As the y coordinate would have to be shifted all the way up first.
            // This explains the weird ordering of rendered elements.
            if (properties.distance && !pos.w.isNaN()) {
                it.matrixStack.matrix {
                    y -= (mc.textRenderer.fontHeight) / 2f
                    // `pos.w` is the distance of the world coordinate to the near plane (projection distance).
                    // It's not the same distance as that of the entity to the player, but it's close enough and saves us some calculations.
                    // It has the side effect of the distance changing as the player rotates their camera.
                    val distText = "${pos.w.roundToInt()}m"
                    val distW = mc.textRenderer.getWidth(distText) / 4f
                    it.matrixStack.translate(
                        pos.x.toDouble() - distW,
                        (y + mc.textRenderer.fontHeight).toDouble(),
                        0.0
                    )
                    it.matrixStack.scale(0.5f, 0.5f, 0f)
                    mc.textRenderer.drawWithShadow(it.matrixStack, distText, 0f, 0f, properties.colour.asARGB())
                }
            }

            if (properties.health && entity is LivingEntity) {
                // Shift everything else up by 2.5 pixels, which is the height of the health bar.
                y -= 2.5f
                drawHealthBar(
                    bufferBuilder,
                    width,
                    pos,
                    y + mc.textRenderer.fontHeight,
                    matrix,
                    fHWidth,
                    entity,
                    it.matrixStack,
                    properties.colour
                )
            }

            // Draw the nametag itself
            mc.textRenderer.drawWithShadow(it.matrixStack, text, pos.x - width / 2, y, colour)
            y -= mc.textRenderer.fontHeight

            properties.items.renderer(it, pos, y, properties.colour.a, colour, entity)
        }
        renderQueue = null
    })

    private fun drawHealthBar(
        bufferBuilder: BufferBuilder,
        width: Int,
        pos: Vector4f,
        y: Float,
        matrix: Matrix4f?,
        fHWidth: Float,
        entity: LivingEntity,
        matrices: MatrixStack,
        colour: Colour
    ) {
        with(bufferBuilder) {
            val xOffset = -width / 2
            val x = pos.x + xOffset
            val z = pos.w
            fun fill(r: Float, g: Float, b: Float, a: Float, width: Float, height: Float = 2f) {
                vertex(matrix, x, y + height, z).color(r, g, b, a).next()
                vertex(matrix, x + width, y + height, z).color(r, g, b, a).next()
                vertex(matrix, x + width, y, z).color(r, g, b, a).next()
                vertex(matrix, x, y, z).color(r, g, b, a).next()
            }

            val absorption = entity.absorptionAmount

            RenderSystem.enableBlend()
            RenderSystem.disableTexture()
            RenderSystem.defaultBlendFunc()

            // We never want the bar width to be extremely small, so we limit it to being as big as the health text.
            val barWidth = (width.toFloat() - fHWidth).coerceAtLeast(fHWidth) - 1

            begin(GL11.GL_QUADS, VertexFormats.POSITION_COLOR)
            fill(0f, 0f, 0f, colour.a * 0.5f, barWidth, 2.5f)
            fill(1f, 0f, 0f, colour.a * 0.7f, barWidth * (entity.health / entity.maxHealth))
            if (absorption > 0) {
                fill(1f, .85f, 0f, colour.a * 0.7f, barWidth * (absorption / entity.maxHealth).coerceAtMost(1f))
            }
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
                mc.textRenderer.drawWithShadow(
                    matrices,
                    "${(entity.health + absorption).roundToInt()}",
                    0f,
                    textY,
                    colour.asARGB()
                )
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
        enum class Items(val renderer: (RenderGuiEvent, Vector4f, Float, Float, Int, Entity) -> Unit) {
            NONE({ _, _, _, _, _, _ -> Unit }),
            JUST_ITEMS({ event, pos, y, alpha, colour, entity ->
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
                    event.matrixStack.translate(pos.x + 8.0, y.toDouble(), 150.0)
                    event.matrixStack.scale(16.0F, -16.0F, 16.0F)
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

                event.matrixStack.matrix {
                    event.matrixStack.translate(
                        pos.x - equipped.size * 16.0 * 0.5/*centering*/,
                        y.toDouble() - 8.0,
                        0.0
                    )

                    equipped.forEach {
                        item.renderGuiItemOverlay(stack = it, matrices = event.matrixStack)
                        event.matrixStack.translate(16.0, 0.0, 0.0)
                    }

                }

                RenderSystem.disableAlphaTest()
                RenderSystem.disableRescaleNormal()
            }),
            ITEMS_AND_ENCHANTS({ event, pos, y, alpha, colour, entity ->
                JUST_ITEMS.renderer(event, pos, y, alpha, colour, entity)
                val y = y - 16 // Items are 16 pixels in size, we're going to draw enchantments above them.
                val equipped = entity.itemsEquipped.filter { !it.isEmpty }

                event.matrixStack.matrix {
                    event.matrixStack.translate(
                        pos.x - equipped.size * 16.0 * 0.5/*centering*/,
                        y.toDouble(),
                        0.0
                    )
                    event.matrixStack.scale(0.5f, 0.5f, 1f)

                    equipped.forEach {
                        val enchantments = it.enchantments
                        var y = 0f
                        (0..enchantments.size)
                            .map { enchantments.getCompound(it) }
                            .forEach {
                                val id = it.getString("id")
                                val lvl = it.getInt("lvl")
                                Registry.ENCHANTMENT.get(Identifier.tryParse(id))?.let {
                                    enchantmentMap[it]?.let { name ->
                                        val lvl = TranslatableText("enchantment.level.$lvl").formatted(Formatting.GRAY)
                                        val text = name.shallowCopy().append(lvl)
                                        mc.textRenderer.drawWithShadow(event.matrixStack, text, 0f, y, colour)
                                        y -= mc.textRenderer.fontHeight
                                    }
                                }
                            }
                        event.matrixStack.translate(16.0 * 2.0 /*scale*/, 0.0, 0.0)
                    }
                }
            });

            companion object {
                val syllables = Regex("[aeiou]")
                val enchantmentMap by lazy {
                    Registry.ENCHANTMENT.entries.map {
                        val builder = StringBuilder()
                        TranslatableText(it.value.translationKey).visit {
                            it.split(" ").let {
                                if (it.size == 3) { // The enchantment is three words - we abbreviate instead of first 3 letters
                                    builder.append("${it[0][0]}${it[1][0]}${it[2][0]}")
                                    return@visit StringVisitable.TERMINATE_VISIT
                                }
                            }
                            val str = it.replace(syllables, "")
                            val v = 3 - builder.length
                            if (v <= 0)
                                StringVisitable.TERMINATE_VISIT
                            else
                                Optional.empty<net.minecraft.util.Unit>().also {
                                    builder.append(str.take(v))
                                }
                        }
                        val short = builder.toString()

                        it.value to LiteralText(short).let { text ->
                            if (it.value.isCursed)
                                text.formatted(Formatting.RED)
                            else
                                text
                        }
                    }.toMap()
                }
            }
        }
    }

    /**
     * Simplified copy of [ItemRenderer.renderGuiItemOverlay] but taking a [MatrixStack] instead of creating an empty one
     *
     * Renders the overlay for items in GUIs, including the damage bar and the item count.
     *
     * @param countLabel a label for the stack; if null, the stack count is drawn instead
     */
    fun ItemRenderer.renderGuiItemOverlay(
        renderer: TextRenderer = mc.textRenderer,
        stack: ItemStack,
        matrices: MatrixStack
    ) {
        if (!stack.isEmpty) {
            val matrix = matrices.peek().model

            fun BufferBuilder.drawQuad(x: Float, y: Float, width: Int, height: Int, r: Int, g: Int, b: Int, a: Int) {
                vertex(matrix, x, y + height, 0f).color(r, g, b, a).next()
                vertex(matrix, x + width, y + height, 0f).color(r, g, b, a).next()
                vertex(matrix, x + width, y, 0f).color(r, g, b, a).next()
                vertex(matrix, x, y, 0f).color(r, g, b, a).next()
            }

            if (stack.count != 1) {
                val string = stack.count.toString()
                val z = (zOffset + 200.0f).toDouble()
                matrices.translate(0.0, 0.0, z)
                renderer.drawWithShadow(
                    matrices,
                    string,
                    (19 - 2 - renderer.getWidth(string)).toFloat(),
                    (6 + 3).toFloat(),
                    16777215,
                )
                matrices.translate(0.0, 0.0, -z)
            }
            if (stack.isDamaged) {
                RenderSystem.disableDepthTest()
                RenderSystem.disableTexture()
                RenderSystem.disableAlphaTest()
                RenderSystem.disableBlend()
                val tessellator = Tessellator.getInstance()
                val bufferBuilder = tessellator.buffer
                val f = stack.damage.toFloat()
                val g = stack.maxDamage.toFloat()
                val h = Math.max(0.0f, (g - f) / g)
                val i = Math.round(13.0f - f * 13.0f / g)
                val j = MathHelper.hsvToRgb(h / 3.0f, 1.0f, 1.0f)

                bufferBuilder.begin(GL11.GL_QUADS, VertexFormats.POSITION_COLOR)
                bufferBuilder.drawQuad(
                    2f, 13f,
                    13, 2,
                    0, 0, 0, 255
                )
                bufferBuilder.drawQuad(
                    2f, 13f,
                    i, 1,
                    j shr 16 and 255,
                    j shr 8 and 255,
                    j and 255,
                    255
                )
                bufferBuilder.end()
                BufferRenderer.draw(bufferBuilder)

                RenderSystem.enableBlend()
                RenderSystem.enableAlphaTest()
                RenderSystem.enableTexture()
                RenderSystem.enableDepthTest()
            }
            val clientPlayerEntity = MinecraftClient.getInstance().player
            val k = clientPlayerEntity?.itemCooldownManager?.getCooldownProgress(
                stack.item,
                MinecraftClient.getInstance().tickDelta
            ) ?: 0.0f
            if (k > 0.0f) {
                RenderSystem.disableDepthTest()
                RenderSystem.disableTexture()
                RenderSystem.enableBlend()
                RenderSystem.defaultBlendFunc()
                val tessellator2 = Tessellator.getInstance()
                val bufferBuilder = tessellator2.buffer
                bufferBuilder.begin(GL11.GL_QUADS, VertexFormats.POSITION_COLOR)
                bufferBuilder.drawQuad(
                    0f, MathHelper.floor(16.0f * (1.0f - k)).toFloat(),
                    16, MathHelper.ceil(16.0f * k),
                    255,
                    255,
                    255,
                    127
                )
                bufferBuilder.end()
                BufferRenderer.draw(bufferBuilder)

                RenderSystem.enableTexture()
                RenderSystem.enableDepthTest()
            }
        }
    }

}
