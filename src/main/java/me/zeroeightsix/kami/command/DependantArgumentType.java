package me.zeroeightsix.kami.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

public abstract class DependantArgumentType<T, D> implements ArgumentType<T> {

    protected final ArgumentType<D> dependantType;
    protected final String dependantArgument;
    private final int shiftWords;

    public DependantArgumentType(ArgumentType<D> dependantType, String dependantArgument, int shift) {
        this.dependantType = dependantType;
        this.dependantArgument = dependantArgument;
        this.shiftWords = shift;
    }

    protected D findDependencyValue(StringReader reader) throws CommandSyntaxException {
        StringReader copy = new StringReader(reader);
        rewind(copy, shiftWords);
        return dependantType.parse(copy);
    }

    protected <S> D findDependencyValue(CommandContext<S> context, Class<D> clazz) {
        return context.getArgument(dependantArgument, clazz);
    }

    private void rewind(StringReader reader, int words) {
        reader.setCursor(Math.max(0, reader.getCursor() - 1));
        while (words > 0) {
            reader.setCursor(Math.max(0, reader.getCursor() - 1)); // Move to the end of the previous argument
            // Move to the front of the previous argument
            while (reader.getCursor() > 0 && reader.peek() != ' ') {
                reader.setCursor(reader.getCursor() - 1);
            }

            words--;
        }
        reader.skip(); // We just found a space; skip it.
    }

}
