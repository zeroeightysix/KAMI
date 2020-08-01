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

    public static MutableText i(MutableText text) {
        return f(Formatting.ITALIC, text);
    }

    public static MutableText b(MutableText text) {
        return f(Formatting.BOLD, text);
    }

    public static MutableText obf(MutableText text) {
        return f(Formatting.OBFUSCATED, text);
    }

    public static MutableText strike(MutableText text) {
        return f(Formatting.STRIKETHROUGH, text);
    }

    public static MutableText r(MutableText text) {
        return f(Formatting.RESET, text);
    }

    /**
     * Produces a formatted version of the provided text
     * @param formatting
     * @param text
     * @return
     */
    public static MutableText f(Formatting formatting, MutableText text) {
        return text.formatted(formatting);
    }

    /**
     * Produces a formatted, literal text
     * @param formatting    the formatting to use
     * @param string        the string to use
     * @return              the created text
     */
    public static MutableText flit(Formatting formatting, String string) {
        return f(formatting, lit(string));
    }

    public static MutableText append(MutableText... texts) {
        Iterator<MutableText> iterator = Arrays.stream(texts).iterator();
        MutableText text = iterator.next();
        while (iterator.hasNext()) {
            //Text no longer has an append function
            text = text.append(iterator.next());
        }
        return text;
    }

}
