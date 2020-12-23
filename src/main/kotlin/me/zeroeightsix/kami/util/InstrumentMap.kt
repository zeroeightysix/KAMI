package me.zeroeightsix.kami.util

import net.minecraft.block.enums.Instrument
import net.minecraft.util.math.BlockPos
import java.util.EnumMap

class InstrumentMap {
    private val instruments = EnumMap<Instrument, Array<BlockPos?>>(
        Instrument::class.java
    )

    operator fun get(instrument: Instrument): Array<BlockPos?> {
        return instruments.getOrPut(instrument) { arrayOfNulls(25) }
    }

    fun add(instrument: Instrument, note: Int, pos: BlockPos?) {
        instruments.getOrPut(instrument) { arrayOfNulls(25) }[note] = pos
    }
}
