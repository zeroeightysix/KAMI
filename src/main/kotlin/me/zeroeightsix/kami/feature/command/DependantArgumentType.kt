package me.zeroeightsix.kami.feature.command

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException

abstract class DependantArgumentType<T, D>(
    private val dependantType: ArgumentType<D>,
    private val dependantArgument: String,
    private val shiftWords: Int
) : ArgumentType<T> {
    @Throws(CommandSyntaxException::class)
    protected fun findDependencyValue(reader: StringReader?): D {
        val copy = StringReader(reader)
        rewind(copy, shiftWords)
        return dependantType.parse(copy)
    }

    protected fun <S> findDependencyValue(context: CommandContext<S>, clazz: Class<D>): D {
        return context.getArgument(dependantArgument, clazz)
    }

    private fun rewind(reader: StringReader, words: Int) {
        var words = words
        reader.cursor = 0.coerceAtLeast(reader.cursor - 1)
        while (words > 0) {
            reader.cursor = 0.coerceAtLeast(reader.cursor - 1) // Move to the end of the previous argument
            // Move to the front of the previous argument
            while (reader.cursor > 0 && reader.peek() != ' ') {
                reader.cursor = reader.cursor - 1
            }
            words--
        }
        reader.skip() // We just found a space; skip it.
    }

}