package me.zeroeightsix.kami.util;

import net.minecraft.block.enums.Instrument;
import net.minecraft.util.math.BlockPos;

public class InstrumentMap {
    private BlockPos[] snare = new BlockPos[25];
    private BlockPos[] hat = new BlockPos[25];
    private BlockPos[] bassdrum = new BlockPos[25];
    private BlockPos[] flute = new BlockPos[25];
    private BlockPos[] chime = new BlockPos[25];
    private BlockPos[] guitar = new BlockPos[25];
    private BlockPos[] harp = new BlockPos[25];
    private BlockPos[] banjo = new BlockPos[25];
    private BlockPos[] bass = new BlockPos[25];
    private BlockPos[] pling = new BlockPos[25];
    private BlockPos[] bit = new BlockPos[25];
    private BlockPos[] bell = new BlockPos[25];
    private BlockPos[] cowBell = new BlockPos[25];
    private BlockPos[] xylophone = new BlockPos[25];
    private BlockPos[] ironXylophone = new BlockPos[25];
    private BlockPos[] dideridoo = new BlockPos[25];
    private BlockPos[] other = new BlockPos[25]; // In case Mojang adds new instruments before KAMI updates

    public InstrumentMap() { /* We don't have anything to construct. */}

    public BlockPos[] get(Instrument instrument) {
        switch (instrument) {
            case BIT:
                return this.bit;
            case BASS:
                return this.bass;
            case BELL:
                return this.bell;
            case BANJO:
                return this.banjo;
            case HAT:
                return this.hat;
            case HARP:
                return this.harp;
            case CHIME:
                return this.chime;
            case FLUTE:
                return this.flute;
            case PLING:
                return this.pling;
            case SNARE:
                return this.snare;
            case GUITAR:
                return this.guitar;
            case BASEDRUM:
                return this.bassdrum;
            case COW_BELL:
                return this.cowBell;
            case XYLOPHONE:
                return this.xylophone;
            case DIDGERIDOO:
                return this.dideridoo;
            case IRON_XYLOPHONE:
                return this.ironXylophone;
            default:
                return this.other;
        }
    }
    public void add(Instrument instrument, int note, BlockPos pos) {
        switch (instrument) {
            case BIT:
                this.bit[note] = pos;
                break;
            case BASS:
                this.bass[note] = pos;
                break;
            case BELL:
                this.bell[note] = pos;
                break;
            case BANJO:
                this.banjo[note] = pos;
                break;
            case HAT:
                this.hat[note] = pos;
                break;
            case HARP:
                this.harp[note] = pos;
                break;
            case CHIME:
                this.chime[note] = pos;
                break;
            case FLUTE:
                this.flute[note] = pos;
                break;
            case PLING:
                this.pling[note] = pos;
                break;
            case SNARE:
                this.snare[note] = pos;
                break;
            case GUITAR:
                this.guitar[note] = pos;
                break;
            case BASEDRUM:
                this.bassdrum[note] = pos;
                break;
            case COW_BELL:
                this.cowBell[note] = pos;
                break;
            case XYLOPHONE:
                this.xylophone[note] = pos;
                break;
            case DIDGERIDOO:
                this.dideridoo[note] = pos;
                break;
            case IRON_XYLOPHONE:
                this.ironXylophone[note] = pos;
                break;
            default:
                this.other[note] = pos;
        }
    }
}
