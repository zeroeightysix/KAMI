package me.zeroeightsix.kami.world

import me.zeroeightsix.kami.mc
import net.minecraft.client.MinecraftClient.IS_SYSTEM_MAC
import net.minecraft.client.gl.Framebuffer
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.RenderPhase
import net.minecraft.client.render.VertexFormat
import net.minecraft.client.render.VertexFormats
import net.minecraft.client.texture.SpriteAtlasTexture
import org.lwjgl.opengl.GL11

object KamiRenderLayers {
    private val smoothModel = RenderPhase.ShadeModel(true)
    private val disableLightmap = RenderPhase.Lightmap(false)
    private val mipmapTexture = RenderPhase.Texture(SpriteAtlasTexture.BLOCK_ATLAS_TEX, false, true)

    @Suppress("INACCESSIBLE_TYPE")
    val solidFiltered: RenderLayer = RenderLayer.of(
        "kami_solid_filtered",
        VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL,
        GL11.GL_QUADS,
        2097152 / 2,
        true,
        false,
        RenderLayer.MultiPhaseParameters.builder()
            .shadeModel(smoothModel)
            .lightmap(disableLightmap)
            .texture(mipmapTexture)
            .build(false)
    )

    @Suppress("INACCESSIBLE_TYPE")
    val solidFilteredOutline: RenderLayer = RenderLayer.of(
        "kami_solid_filtered_outline",
        VertexFormats.POSITION_COLOR_TEXTURE,
        GL11.GL_QUADS,
        2097152 / 4,
        true,
        false,
        RenderLayer.MultiPhaseParameters.builder()
            .lightmap(disableLightmap)
            .texture(mipmapTexture)
            .build(false)
    )

    val layers = mapOf(
        solidFiltered to VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL,
        solidFilteredOutline to VertexFormats.POSITION_COLOR_TEXTURE
    )

    val filteredFramebuffer by lazy {
        Framebuffer(mc.window.framebufferWidth, mc.window.framebufferHeight, true, IS_SYSTEM_MAC)
    }
}