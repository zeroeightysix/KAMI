package me.zeroeightsix.kami.mixin.client;

import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.event.events.ChunkEvent;
import net.minecraft.client.world.ClientChunkManager;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ClientChunkManager.class)
public class MixinClientChunkManager {

    @Inject(
            method = "unload",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientChunkManager;positionEquals(Lnet/minecraft/world/chunk/WorldChunk;II)Z"),
            locals = LocalCapture.CAPTURE_FAILHARD,
            cancellable = true
    )
    public void onUnload(int chunkX, int chunkZ, CallbackInfo ci, WorldChunk worldChunk) {
        ChunkEvent.Unload unload = new ChunkEvent.Unload(worldChunk);
        KamiMod.EVENT_BUS.post(unload);
        if (unload.isCancelled()) ci.cancel();
    }

}
