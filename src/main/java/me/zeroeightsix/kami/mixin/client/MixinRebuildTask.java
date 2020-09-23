package me.zeroeightsix.kami.mixin.client;

import me.zeroeightsix.kami.world.KamiRenderLayers;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.chunk.BlockBufferBuilderStorage;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.client.render.chunk.ChunkOcclusionDataBuilder;
import net.minecraft.client.render.chunk.ChunkRendererRegion;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import static org.lwjgl.opengl.GL11C.GL_QUADS;

@Mixin(targets = "net.minecraft.client.render.chunk.ChunkBuilder$BuiltChunk$RebuildTask")
public class MixinRebuildTask {

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;translate(DDD)V", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
    public void onPop(float f, float g, float h, ChunkBuilder.ChunkData chunkData, BlockBufferBuilderStorage blockBufferBuilderStorage, CallbackInfoReturnable<Set<BlockEntity>> cir, int i, BlockPos blockPos, BlockPos blockPos2, ChunkOcclusionDataBuilder chunkOcclusionDataBuilder, Set set, ChunkRendererRegion chunkRendererRegion, MatrixStack matrixStack, Random random, BlockRenderManager blockRenderManager, Iterator var15, BlockPos blockPos3, BlockState blockState, RenderLayer renderLayer2, BufferBuilder bufferBuilder2) {
        if (blockState.getBlock() == Blocks.DIAMOND_ORE) {
            RenderLayer layer = KamiRenderLayers.INSTANCE.getSolidFiltered();
            BufferBuilder builder = blockBufferBuilderStorage.get(layer);
            IChunkData iChunkData = (IChunkData) chunkData;
            if (iChunkData.getInitializedLayers().add(layer)) {
                builder.begin(GL_QUADS, VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL);
            }

            if (blockRenderManager.renderBlock(blockState, blockPos3, chunkRendererRegion, matrixStack, builder, false, random)) {
                iChunkData.setEmpty(false);
                iChunkData.getNonEmptyLayers().add(layer);
            }
        }
    }

}
