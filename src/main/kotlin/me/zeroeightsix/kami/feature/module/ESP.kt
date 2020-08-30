package me.zeroeightsix.kami.feature.module

import com.google.gson.JsonSyntaxException
import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting
import me.zeroeightsix.kami.Colour
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.util.Target
import me.zeroeightsix.kami.util.Targets
import net.minecraft.client.gl.Framebuffer
import net.minecraft.client.gl.ShaderEffect
import net.minecraft.client.render.OutlineVertexConsumerProvider
import net.minecraft.client.render.RenderPhase
import net.minecraft.util.Identifier
import java.io.IOException

@Module.Info(name = "ESP", description = "Draws outlines around targetted entities", category = Module.Category.RENDER)
object ESP : Module() {

    var outlineShader: ShaderEffect? = null
    var entityOutlinesFramebuffer: Framebuffer? = null

    private val SHARP_OUTLINE_TARGET = RenderPhase.Target("sharp_outline_target", {
        entityOutlinesFramebuffer?.beginWrite(false)
    }, {
        mc.framebuffer.beginWrite(true)
    })

    // Lazy so the init happens when everything is set up; otherwise entityVertexConsumers will be null
    val outlineConsumerProvider by lazy {
        OutlineVertexConsumerProvider(mc.bufferBuilders.entityVertexConsumers)
    }

    @Setting
    var targets = Targets(
        mapOf(
            Target.ALL_PLAYERS to Colour.WHITE
        )
    )

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
