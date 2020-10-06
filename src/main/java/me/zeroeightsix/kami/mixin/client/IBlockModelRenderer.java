package me.zeroeightsix.kami.mixin.client;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.BitSet;
import java.util.List;

@Mixin(BlockModelRenderer.class)
public interface IBlockModelRenderer {

    @Invoker
    void callRenderQuad(BlockRenderView blockRenderView, BlockState blockState, BlockPos blockPos, VertexConsumer vertexConsumer, MatrixStack.Entry entry, BakedQuad bakedQuad, float f, float g, float h, float i, int j, int k, int l, int m, int n);

}
