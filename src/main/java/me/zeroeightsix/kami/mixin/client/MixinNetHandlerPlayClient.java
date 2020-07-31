package me.zeroeightsix.kami.mixin.client;

import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.event.events.ChunkEvent;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.packet.ChunkDataS2CPacket;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

/**
 * @author 086
 */
@Mixin(ClientPlayNetworkHandler.class)
public class MixinNetHandlerPlayClient {

    @Inject(method = "onChunkData",
            at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/client/world/ClientChunkManager;loadChunkFromPacket(Lnet/minecraft/world/World;IILnet/minecraft/util/PacketByteBuf;Lnet/minecraft/nbt/CompoundTag;IZ)Lnet/minecraft/world/chunk/WorldChunk;"),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void read(ChunkDataS2CPacket packet, CallbackInfo ci, int i, int j, WorldChunk worldChunk) {
        KamiMod.EVENT_BUS.post(new ChunkEvent(worldChunk, packet));
    }

}
