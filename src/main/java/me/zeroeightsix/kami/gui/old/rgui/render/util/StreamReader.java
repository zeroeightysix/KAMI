package me.zeroeightsix.kami.gui.old.rgui.render.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.StringJoiner;

/**
 * Reads from an InputStream
 *
 * @author Brady
 * @since 2/15/2017 12:00 PM
 */
public final class StreamReader {

    /**
     * Stream being read
     */
    private final InputStream stream;

    public StreamReader(InputStream stream) {
        this.stream = stream;
    }

    /**
     * Reads the stream and returns the output
     *
     * @return The stream's output
     */
    public final String read() {
        StringJoiner joiner = new StringJoiner("\n");
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(stream));
            String line;
            while ((line = br.readLine()) != null)
                joiner.add(line);

            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return joiner.toString();
    }
}