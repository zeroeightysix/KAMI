package me.zeroeightsix.kami.util;

import net.minecraft.block.enums.Instrument;
import net.minecraft.util.math.BlockPos;

import java.util.EnumMap;

public class InstrumentMap {
    private EnumMap<Instrument, BlockPos[]> instruments = new EnumMap<>(Instrument.class);

    public InstrumentMap() {
        instruments.put(Instrument.SNARE, new BlockPos[25]);
        instruments.put(Instrument.HAT, new BlockPos[25]);
        instruments.put(Instrument.BASEDRUM, new BlockPos[25]);
        instruments.put(Instrument.FLUTE, new BlockPos[25]);
        instruments.put(Instrument.CHIME, new BlockPos[25]);
        instruments.put(Instrument.GUITAR, new BlockPos[25]);
        instruments.put(Instrument.HARP, new BlockPos[25]);
        instruments.put(Instrument.BANJO, new BlockPos[25]);
        instruments.put(Instrument.BASS, new BlockPos[25]);
        instruments.put(Instrument.PLING, new BlockPos[25]);
        instruments.put(Instrument.BIT, new BlockPos[25]);
        instruments.put(Instrument.BELL, new BlockPos[25]);
        instruments.put(Instrument.COW_BELL, new BlockPos[25]);
        instruments.put(Instrument.XYLOPHONE, new BlockPos[25]);
        instruments.put(Instrument.IRON_XYLOPHONE, new BlockPos[25]);
        instruments.put(Instrument.DIDGERIDOO, new BlockPos[25]);

    }

    public BlockPos[] get(Instrument instrument) {
        return instruments.get(instrument);
    }

    public void add(Instrument instrument, int note, BlockPos pos) {
        instruments.get(instrument)[note] = pos;
    }
}
