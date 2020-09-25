package me.zeroeightsix.kami.feature.module

import com.google.gson.JsonSyntaxException
import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting
import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.Colour
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.event.ChunkCullingEvent
import me.zeroeightsix.kami.util.BlockTarget
import me.zeroeightsix.kami.util.BlockTargets
import me.zeroeightsix.kami.util.EntityTarget
import me.zeroeightsix.kami.util.EntityTargets
import net.minecraft.client.gl.Framebuffer
import net.minecraft.client.gl.ShaderEffect
import net.minecraft.client.render.OutlineVertexConsumerProvider
import net.minecraft.util.Identifier
import java.io.IOException

/**
 * @see me.zeroeightsix.kami.mixin.client.MixinWorldRenderer
 */
@Module.Info(name = "ESP", description = "Draws outlines around targetted entities", category = Module.Category.RENDER)
object ESP : Module() {

    var outlineShader: ShaderEffect? = null
    var entityOutlinesFramebuffer: Framebuffer? = null

    // Lazy so the init happens when everything is set up; otherwise entityVertexConsumers will be null
    val outlineConsumerProvider by lazy {
        OutlineVertexConsumerProvider(mc.bufferBuilders.entityVertexConsumers)
    }

    @Setting(name = "Entities")
    var targets = EntityTargets(
        mapOf(
            EntityTarget.ALL_PLAYERS to Colour.WHITE
        )
    )

    @Setting(name = "Block entities")
    var blockTargets = BlockTargets(
        mapOf(
            BlockTarget.CHESTS to Colour(1f, 0.92f, 0.81f, 0.28f) // Gold brownish
        )
    )

    @EventHandler
    val cullingListener = Listener<ChunkCullingEvent>({
        it.chunkCulling = false // TODO: Only =false if ESP is actively showing blocks that require chunk culling to be off
    })

    fun closeShader() = outlineShader?.close()

    fun loadOutlineShader() {
        closeShader()
        val identifier = Identifier("kami", "shaders/post/entity_sharp_outline.json")
        try {
            outlineShader = ShaderEffect(
                mc.textureManager,
                mc.resourceManager,
                mc.framebuffer,
                identifier
            ).also {
                it.setupDimensions(
                    mc.window.framebufferWidth,
                    mc.window.framebufferHeight
                )
                entityOutlinesFramebuffer = it.getSecondaryTarget("final")
            }
        } catch (var3: IOException) {
            KamiMod.log.warn("Failed to load shader: {}", identifier, var3)
            this.outlineShader = null
            entityOutlinesFramebuffer = null
        } catch (var4: JsonSyntaxException) {
            KamiMod.log.warn("Failed to parse shader: {}", identifier, var4)
            this.outlineShader = null
            entityOutlinesFramebuffer = null
        }
    }
}
