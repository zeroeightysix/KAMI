package me.zeroeightsix.kami.world

import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.RenderPhase
import net.minecraft.client.render.VertexFormats
import net.minecraft.client.texture.SpriteAtlasTexture
import org.lwjgl.opengl.GL11

object KamiRenderLayers {
    private val smoothModel = RenderPhase.ShadeModel(true)
    private val enableLightmap = RenderPhase.Lightmap(true)
    private val mipmapTexture = RenderPhase.Texture(SpriteAtlasTexture.BLOCK_ATLAS_TEX, false, true)

    val solidFiltered: RenderLayer = RenderLayer.of(
        "kami_solid_filtered",
        VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL,
        GL11.GL_QUADS,
        2097152,
        true,
        false,
        RenderLayer.MultiPhaseParameters.builder()
            .shadeModel(smoothModel)
            .lightmap(enableLightmap)
            .texture(mipmapTexture)
            .build(false)
    )

    val layers = listOf(solidFiltered)
}