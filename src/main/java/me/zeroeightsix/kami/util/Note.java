package me.zeroeightsix.kami.util;

public class Note {
    int note;
    int track;

    public Note(int note, int track) {
        this.note = note;
        this.track = track;
    }

    public int getNote() {
        return note;
    }

    public int getNotebotNote() {
        return getNotebotKey(note);
    }

    public int getTrack() {
        return track;
    }

    @Override
    public String toString() {
        return getKey(note) + "[" + track + "]";
    }

    public static String getKey(int note) {
        String[] keys = new String[]{
                "F#", "G", "G#", "A", "A#", "B", "C", "C#", "D", "D#", "E", "F",
                "F#2", "G2", "G#2", "A2", "A#2", "B2", "C2", "C#2", "D2", "D#2", "E2", "F2",
                "F#3"
        };
        return keys[getNotebotKey(note)];
    }

    private static int getNotebotKey(int note) {
        /**
         *                               "MIDI NOTES"
         *  "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B",
         *  "C2", "C#2", "D2", "D#2", "E2", "F2", "F#2", "G2", "G#2", "A2", "A#2", "B2",
         *   "C3", "C#3", "D3", "D#3", "E3", "F3", "F#3", "G3", "G#3", "A3", "A#3", "B3"
         */
        int k = ((note - 6) % 24);
        return (k < 0) ? 24 + k : k;
    }
}
