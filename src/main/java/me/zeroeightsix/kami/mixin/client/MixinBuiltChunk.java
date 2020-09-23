package me.zeroeightsix.kami.mixin.client;

import me.zeroeightsix.kami.world.KamiRenderLayers;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.chunk.BlockBufferBuilderStorage;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.stream.Collectors;

@Mixin(ChunkBuilder.BuiltChunk.class)
public class MixinBuiltChunk {

    @Shadow @Final private Map<RenderLayer, VertexBuffer> buffers;

    @Inject(method = "<init>", at = @At("TAIL"))
    public void onInit(ChunkBuilder outer, CallbackInfo ci) {
        this.buffers.putAll(KamiRenderLayers.INSTANCE.getLayers().stream().collect(Collectors.toMap(renderLayer -> renderLayer, renderLayer -> new VertexBuffer(VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL))));
    }

}
