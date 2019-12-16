package me.zeroeightsix.kami.event.events;

import me.zeroeightsix.kami.event.KamiEvent;
import net.minecraft.client.network.packet.ChunkDataS2CPacket;
import net.minecraft.world.chunk.Chunk;

/**
 * @author 086
 */
public class ChunkEvent extends KamiEvent {
    private Chunk chunk;
    private ChunkDataS2CPacket packet;

    public ChunkEvent(Chunk chunk, ChunkDataS2CPacket packet) {
        this.chunk = chunk;
        this.packet = packet;
    }

    public Chunk getChunk() {
        return chunk;
    }

    public ChunkDataS2CPacket getPacket() {
        return packet;
    }
}
