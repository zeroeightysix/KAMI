package me.zeroeightsix.kami.util

import java.io.File
import java.io.IOException
import java.util.ArrayList
import java.util.TreeMap
import javax.sound.midi.InvalidMidiDataException
import javax.sound.midi.MetaMessage
import javax.sound.midi.MidiEvent
import javax.sound.midi.MidiSystem
import javax.sound.midi.Sequence
import javax.sound.midi.ShortMessage
import net.minecraft.block.enums.Instrument

object MidiParser {
    private const val SET_TEMPO = 0x51
    private const val TIME_SIGNATURE = 0x58
    private const val PITCH_CHANGE = 0xC0
    private const val QUARTER_NOTE_IN_MICROSECONDS = 0x07A120

    /**
     * Takes a midi file as a string, parses it, and returns what notes play at times in a Map.
     *
     * @param filename The path to the file to load, relative to the current working directory.
     * @return A sorted [Map] with times for keys and respective notes for values.
     * @throws InvalidMidiDataException If the file data was invalid from the perspective of the MIDI specification.
     * @throws IOException              If an IO exception occurred.
     */
    @Throws(InvalidMidiDataException::class, IOException::class)
    fun parse(filename: String): Pair<TreeMap<Long, ArrayList<Note>>, TreeMap<Int, Instrument>> {
        val sequence: Sequence? = MidiSystem.getSequence(File(filename))
        val channelList = TreeMap<Int, Instrument>()
        var maxStamp: Long = 0
        val noteSequence = TreeMap<Long, ArrayList<Note>>()
        val resolution = sequence!!.resolution.toDouble()
        for (track in sequence.tracks) {
            for (i in 0 until track.size()) {
                val event = track[i]
                val types = getTypes(event)
                for (eventType in types) {
                    val shortMessage =
                        if (event.message is ShortMessage) event.message as ShortMessage else ShortMessage()
                    if (eventType == MidiEventType.NOTE_ON) {
                        val note = shortMessage.data1 % 36
                        val tick = event.tick
                        val channel = shortMessage.channel
                        val time =
                            (tick * (QUARTER_NOTE_IN_MICROSECONDS.toDouble() / resolution) / 1000.0 + 0.5).toLong()
                        maxStamp = time.coerceAtLeast(maxStamp)
                        if (!noteSequence.containsKey(time)) noteSequence[time] = ArrayList()
                        if (channelList.size <= 5 && !channelList.keys.contains(channel))
                            channelList[channel] = Instrument.HAT
                        noteSequence[time]!!.add(Note(note, channelList.keys.indexOf(channel)))
                    }
                }
            }
        }
        return noteSequence to channelList
    }

    private fun getTypes(event: MidiEvent): Array<MidiEventType?> {
        val returnValue = arrayOfNulls<MidiEventType>(3)
        if (event.message is ShortMessage) {
            val shortMessage = event.message as ShortMessage
            if (shortMessage.status in PITCH_CHANGE..0xCF) returnValue[0] =
                MidiEventType.PITCH_CHANGE
            if (shortMessage.command == ShortMessage.NOTE_ON) returnValue[1] = MidiEventType.NOTE_ON
        }
        if (event.message is MetaMessage) {
            val metaMessage = event.message as MetaMessage
            if (metaMessage.type == SET_TEMPO) returnValue[2] =
                MidiEventType.SET_TEMPO else if (metaMessage.type == TIME_SIGNATURE) returnValue[2] =
                MidiEventType.TIME_SIGNATURE
        }
        return returnValue
    }
}

class Note(var note: Int, var track: Int) {
    val notebotNote: Int
        get() = getNotebotKey(note)

    override fun toString(): String {
        return getKey(note) + "[" + track + "]"
    }

    companion object {
        private var keys = arrayOf(
            "F#", "G", "G#", "A", "A#", "B", "C", "C#", "D", "D#", "E", "F",
            "F#2", "G2", "G#2", "A2", "A#2", "B2", "C2", "C#2", "D2", "D#2", "E2", "F2",
            "F#3"
        )

        fun getKey(note: Int): String {
            return keys[getNotebotKey(note)]
        }

        private fun getNotebotKey(note: Int): Int {
            /**
             * "MIDI NOTES"
             * "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B",
             * "C2", "C#2", "D2", "D#2", "E2", "F2", "F#2", "G2", "G#2", "A2", "A#2", "B2",
             * "C3", "C#3", "D3", "D#3", "E3", "F3", "F#3", "G3", "G#3", "A3", "A#3", "B3"
             */
            val k = (note - 6) % 24
            return if (k < 0) 24 + k else k
        }
    }
}

private enum class MidiEventType {
    NOTE_ON, TIME_SIGNATURE, SET_TEMPO, PITCH_CHANGE
}