package me.zeroeightsix.kami.util;

import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Arrays;
import java.util.Iterator;

/**
 * A helper class for condensed, formatted literal {@link Text} creation.
 */
public class Texts {

    /**
     * Produces a {@link LiteralText} from the given string
     * @param string
     * @return
     */
    public static LiteralText lit(String string) {
        return new LiteralText(string);
    }

    public static Text i(MutableText text) {
        return f(Formatting.ITALIC, text);
    }

    public static Text b(MutableText text) {
        return f(Formatting.BOLD, text);
    }

    public static Text obf(MutableText text) {
        return f(Formatting.OBFUSCATED, text);
    }

    public static Text strike(MutableText text) {
        return f(Formatting.STRIKETHROUGH, text);
    }

    public static Text r(MutableText text) {
        return f(Formatting.RESET, text);
    }

    /**
     * Produces a formatted version of the provided text
     * @param formatting
     * @param text
     * @return
     */
    public static Text f(Formatting formatting, MutableText text) {
        return text.formatted(formatting);
    }

    /**
     * Produces a formatted, literal text
     * @param formatting    the formatting to use
     * @param string        the string to use
     * @return              the created text
     */
    public static Text flit(Formatting formatting, String string) {
        return (MutableText) f(formatting, lit(string));
    }

    public static Text append(MutableText... texts) {
        Iterator<MutableText> iterator = Arrays.stream(texts).iterator();
        MutableText text = iterator.next();
        while (iterator.hasNext()) {
            //Text no longer has an append function
            text = text.append(iterator.next());
        }
        return text;
    }

}
