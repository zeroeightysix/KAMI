package me.zeroeightsix.kami.mixin.client;

import me.zeroeightsix.kami.Colour;
import me.zeroeightsix.kami.feature.module.ESP;
import me.zeroeightsix.kami.util.OutlineVertexConsumer;
import me.zeroeightsix.kami.world.KamiRenderLayers;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.chunk.BlockBufferBuilderStorage;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.client.render.chunk.ChunkOcclusionDataBuilder;
import net.minecraft.client.render.chunk.ChunkRendererRegion;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockRenderView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.lwjgl.opengl.GL11C.GL_QUADS;

@Mixin(targets = "net.minecraft.client.render.chunk.ChunkBuilder$BuiltChunk$RebuildTask")
public class MixinRebuildTask {

    @Inject(method = "render(FFFLnet/minecraft/client/render/chunk/ChunkBuilder$ChunkData;Lnet/minecraft/client/render/chunk/BlockBufferBuilderStorage;)Ljava/util/Set;", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;translate(DDD)V", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
    public void onPop(float f, float g, float h, ChunkBuilder.ChunkData chunkData, BlockBufferBuilderStorage blockBufferBuilderStorage, CallbackInfoReturnable<Set<BlockEntity>> cir, int i, BlockPos blockPos, BlockPos blockPos2, ChunkOcclusionDataBuilder chunkOcclusionDataBuilder, Set set, ChunkRendererRegion chunkRendererRegion, MatrixStack matrixStack, Random random, BlockRenderManager blockRenderManager, Iterator var15, BlockPos blockPos3, BlockState blockState, RenderLayer renderLayer2, BufferBuilder bufferBuilder2) {
        Block block = blockState.getBlock();
        IChunkData iChunkData = (IChunkData) chunkData;
        VertexConsumer consumer = null;

        RenderLayer solidLayer = null;
        RenderLayer outlineLayer = null;

        ESP.ESPTarget target = ESP.INSTANCE.getBlockTargets().get(block);
        
        if (target == null) return;

        if (target.getSolid()) {
            solidLayer = KamiRenderLayers.INSTANCE.getSolidFiltered();
            BufferBuilder builder = blockBufferBuilderStorage.get(solidLayer);
            if (iChunkData.getInitializedLayers().add(solidLayer)) {
                builder.begin(GL_QUADS, VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL);
            }

            consumer = builder;
        }

        if (target.getOutline()) { // if: has outline
            outlineLayer = KamiRenderLayers.INSTANCE.getSolidFilteredOutline();
            BufferBuilder outlineBuilder = blockBufferBuilderStorage.get(outlineLayer);
            if (iChunkData.getInitializedLayers().add(outlineLayer)) {
                outlineBuilder.begin(GL_QUADS, VertexFormats.POSITION_COLOR_TEXTURE);
            }
            Integer[] outlineColour = target.getOutlineColour().asInts();
            OutlineVertexConsumer outlineVertexConsumer = new OutlineVertexConsumer(
                outlineBuilder,
                outlineColour[1],
                outlineColour[2],
                outlineColour[3],
                outlineColour[0]
            );

            if (consumer != null)
                consumer = VertexConsumers.dual(consumer, outlineVertexConsumer);
            else
                consumer = outlineVertexConsumer;
        }

        if (consumer == null) return;

        if (renderAllSidesUnlitFlat(blockRenderManager, blockState, blockPos3, chunkRendererRegion, matrixStack, consumer, random)) {
            iChunkData.setEmpty(false);
            if (solidLayer != null)
                iChunkData.getNonEmptyLayers().add(solidLayer);
            if (outlineLayer != null)
                iChunkData.getNonEmptyLayers().add(outlineLayer);
        }
    }

    private static boolean renderAllSidesUnlitFlat(BlockRenderManager manager, BlockState blockState, BlockPos blockPos, BlockRenderView blockRenderView, MatrixStack matrixStack, VertexConsumer vertexConsumer, Random random) {
        BlockRenderType blockRenderType = blockState.getRenderType();
        if (blockRenderType != BlockRenderType.MODEL) return false;

        boolean ret = false;

        Vec3d vec3d = blockState.getModelOffset(blockRenderView, blockPos);
        matrixStack.translate(vec3d.x, vec3d.y, vec3d.z);

        long seed = blockState.getRenderingSeed(blockPos);
        BakedModel bakedModel = manager.getModel(blockState);
        for (Direction direction : Direction.values()) {
            random.setSeed(seed);

            List<BakedQuad> list = bakedModel.getQuads(blockState, direction, random);
            if (!list.isEmpty()) {
                renderQuadsFlatUnlit(manager, blockRenderView, blockState, blockPos, 0xf000f0, OverlayTexture.DEFAULT_UV, matrixStack, vertexConsumer, list);
                ret = true;
            }
        }

        return ret;
    }

    private static void renderQuadsFlatUnlit(BlockRenderManager manager, BlockRenderView blockRenderView, BlockState blockState, BlockPos blockPos, int i, int j, MatrixStack matrixStack, VertexConsumer vertexConsumer, List<BakedQuad> list) {
        IBlockModelRenderer renderer = (IBlockModelRenderer) manager.getModelRenderer();
        MatrixStack.Entry model = matrixStack.peek();
        for (BakedQuad quad : list) {
            float quadBrightness = blockRenderView.getBrightness(quad.getFace(), quad.hasShade());
            renderer.callRenderQuad(blockRenderView, blockState, blockPos, vertexConsumer, model, quad, quadBrightness, quadBrightness, quadBrightness, quadBrightness, i, i, i, i, j);
        }
    }

}
