package me.zeroeightsix.kami.mixin.client;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.chunk.ChunkBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

@Mixin(ChunkBuilder.ChunkData.class)
public interface IChunkData {

    @Accessor
    Set<RenderLayer> getInitializedLayers();

    @Accessor
    void setEmpty(boolean empty);

    @Accessor
    Set<RenderLayer> getNonEmptyLayers();

}
