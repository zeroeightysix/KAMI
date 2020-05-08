package me.zeroeightsix.kami.util;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author dominikaaaa
 * Lazy fix used for Java instance of {@link me.zeroeightsix.kami.util.Macro} and {@link me.zeroeightsix.kami.feature.MacroManager}
 * TODO: Port this to Kotlin. I had issues looping through a Java Map in Kotlin, like so:
 *       for ((key1, value) in Macros.macros) {
 */
public class Macros {
    /*
     * Map of all the macros.
     * KeyCode, Actions
     */
    public static Map<Integer, List<String>> macros = new LinkedHashMap<>();
}
