package me.zeroeightsix.kami.event.events

import me.zeroeightsix.kami.event.KamiEvent
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket
import net.minecraft.world.chunk.Chunk

/**
 * @author 086
 */
open class ChunkEvent private constructor(val chunk: Chunk?) : KamiEvent() {
    class Load(chunk: Chunk?, val packet: ChunkDataS2CPacket) : ChunkEvent(chunk)
    class Unload(chunk: Chunk?) : ChunkEvent(chunk)
}
