package me.zeroeightsix.kami.mixin.client;

import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.event.ChunkEvent;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.world.biome.source.BiomeArray;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ClientPlayNetworkHandler.class)
public class MixinNetHandlerPlayClient {

    @Inject(method = "onChunkData",
            at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/client/world/ClientChunkManager;loadChunkFromPacket(IILnet/minecraft/world/biome/source/BiomeArray;Lnet/minecraft/network/PacketByteBuf;Lnet/minecraft/nbt/CompoundTag;IZ)Lnet/minecraft/world/chunk/WorldChunk;"),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void read(ChunkDataS2CPacket packet, CallbackInfo ci, int i, int j, BiomeArray biomeArray, WorldChunk worldChunk) {
        KamiMod.EVENT_BUS.post(new ChunkEvent.Load(worldChunk, packet));
    }

}
