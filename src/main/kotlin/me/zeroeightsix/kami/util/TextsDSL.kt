package me.zeroeightsix.kami.util

import me.zeroeightsix.kami.unreachable
import net.minecraft.text.LiteralText
import net.minecraft.text.MutableText
import net.minecraft.util.Formatting

// Required to scope the LiteralText in the String.invoke extension function
class TextBuilder(string: String) : LiteralText(string) {
    /**
     * Creates a formatted [MutableText] from `this` String, but does not append it to this [TextBuilder]
     */
    operator fun String.invoke(
        formatting: Formatting? = null,
        block: TextBuilder.() -> Unit = {}
    ): MutableText = TextBuilder(this).also {
        formatting?.let { f -> it.formatted = f }
    }.also(block)

    /**
     * Calls the invocation operator overload on this string with no parameters, and adds it to this [TextBuilder].
     */
    operator fun String.unaryPlus() = +this()

    /**
     * Adds `this` [MutableText] to the scoped [TextBuilder]
     */
    operator fun MutableText.unaryPlus(): MutableText = this@TextBuilder.append(this)

    var formatted: Formatting
        get() = unreachable()
        set(value) {
            this.formatted(value)
        }
}

fun text(
    formatting: Formatting? = null,
    root: String = "",
    block: TextBuilder.() -> Unit = {}
) = TextBuilder(root).also {
    formatting?.let { f -> it.formatted = f }
}.also(block)
