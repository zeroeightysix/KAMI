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

object MidiParser {
    const val SET_TEMPO = 0x51
    const val TIME_SIGNATURE = 0x58
    const val PITCH_CHANGE = 0xC0
    const val QUARTER_NOTE_IN_MICROSECONDS = 0x07A120

    /**
     * Takes a midi file as a string, parses it, and returns what notes play at times in a Map.
     *
     * @param filename - Midi File
     * @return - Notes that play at what time
     * @throws InvalidMidiDataException - If the File is corrupt
     * @throws IOException              - If the file isn't found
     */
    @Throws(InvalidMidiDataException::class, IOException::class)
    fun parse(filename: String?, maxChannels: Int): TreeMap<Long, ArrayList<Note>> {
        try {

            val file = File(filename)
            var sequence: Sequence? = MidiSystem.getSequence(file)
            val channelList = ArrayList<Int>()
            var channelPrograms = IntArray(30)
            var maxStamp: Long = 0
            val noteSequence = TreeMap<Long, ArrayList<Note>>()
            val resolution = sequence!!.getResolution().toDouble()
            for (track in sequence.getTracks()) {
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
                            maxStamp = Math.max(time, maxStamp)
                            if (!noteSequence.containsKey(time)) noteSequence[time] = ArrayList()
                            if (channelList.size <= maxChannels && !channelList.contains(channel)) channelList.add(
                                channel
                            )
                            noteSequence[time]!!.add(Note(note, channelList.indexOf(channel)))
                        }
                    }
                }
            }
            return noteSequence
        } catch (e: Exception) {
            return TreeMap<Long, ArrayList<Note>>()
        }
    }

    private fun getTypes(event: MidiEvent): Array<MidiEventType?> {
        val returnValue = arrayOfNulls<MidiEventType>(3)
        if (event.message is ShortMessage) {
            val shortMessage = event.message as ShortMessage
            if (shortMessage.status >= PITCH_CHANGE && shortMessage.status <= 0xCF) returnValue[0] =
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

internal enum class MidiEventType {
    NOTE_ON, TIME_SIGNATURE, SET_TEMPO, PITCH_CHANGE
}
