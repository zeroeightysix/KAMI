package me.zeroeightsix.kami.event.events

import me.zeroeightsix.kami.event.KamiEvent
import net.minecraft.client.network.packet.ChunkDataS2CPacket
import net.minecraft.world.chunk.Chunk

/**
 * @author 086
 */
class ChunkEvent(val chunk: Chunk, val packet: ChunkDataS2CPacket) : KamiEvent()