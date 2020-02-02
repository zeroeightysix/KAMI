package me.zeroeightsix.kami.command

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import me.zeroeightsix.kami.plugin.Plugin
import me.zeroeightsix.kami.plugin.PluginManager
import net.minecraft.server.command.CommandSource
import net.minecraft.text.LiteralText
import java.util.concurrent.CompletableFuture
import java.util.function.Function

class PluginArgumentType: ArgumentType<Plugin> {

    private val examples: MutableCollection<String> = mutableListOf("Aura", "CameraClip", "Flight")
    private val invalidPluginException = DynamicCommandExceptionType(
        Function { `object`: Any -> LiteralText("Unknown module '$`object`'") }
    )

    companion object {
        fun plugin(): PluginArgumentType {
            return PluginArgumentType()
        }
    }

    override fun parse(reader: StringReader?): Plugin {
        val reader: StringReader = reader!!
        val str = reader.readUnquotedString()
        PluginManager.getPlugin(str)?.let {
            return it
        }
        throw invalidPluginException.create(str)
    }

    override fun <S> listSuggestions(
        context: CommandContext<S>?,
        builder: SuggestionsBuilder?
    ): CompletableFuture<Suggestions?>? {
        return CommandSource.suggestMatching(
            PluginManager.plugins.stream().map { obj: Plugin -> obj.name.value },
            builder
        )
    }

    override fun getExamples(): MutableCollection<String> {
        return examples
    }
}