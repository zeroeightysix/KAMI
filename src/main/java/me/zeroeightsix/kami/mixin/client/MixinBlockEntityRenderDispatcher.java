package me.zeroeightsix.kami.mixin.client;

import me.zeroeightsix.kami.Colour;
import me.zeroeightsix.kami.feature.module.ESP;
import me.zeroeightsix.kami.mixin.duck.HotSwappable;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OutlineVertexConsumerProvider;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BlockEntityRenderDispatcher.class)
public abstract class MixinBlockEntityRenderDispatcher {
    @Redirect(method = "net/minecraft/client/render/block/entity/BlockEntityRenderDispatcher.method_23081(Lnet/minecraft/client/render/block/entity/BlockEntityRenderer;Lnet/minecraft/block/entity/BlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/render/block/entity/BlockEntityRenderDispatcher;render(Lnet/minecraft/client/render/block/entity/BlockEntityRenderer;Lnet/minecraft/block/entity/BlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;)V")
    )
    private static <E extends BlockEntity> void onRender(BlockEntityRenderer<E> renderer, E blockEntity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers) {
        if (ESP.INSTANCE.getEnabled()) {
            Colour colour = ESP.INSTANCE.getBlockTargets().belongs(blockEntity);
            if (colour != null) {
                ((HotSwappable) MinecraftClient.getInstance().worldRenderer).swapWhile(() -> {
                    OutlineVertexConsumerProvider provider = ESP.INSTANCE.getEntityOutlineVertexConsumerProvider();
                    provider.setColor((int) (colour.getR() * 255), (int) (colour.getG() * 255), (int) (colour.getB() * 255), (int) (colour.getA() * 255));
                    render(renderer, blockEntity, tickDelta, matrices, provider);
                });
                return;
            }
        }
        render(renderer, blockEntity, tickDelta, matrices, vertexConsumers);
    }

    @Shadow
    private static <T extends BlockEntity> void render(BlockEntityRenderer<T> renderer, T blockEntity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers) {
    }
}
