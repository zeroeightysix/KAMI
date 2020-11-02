package me.zeroeightsix.kami.feature.command

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import me.zeroeightsix.kami.feature.FeatureManager
import me.zeroeightsix.kami.feature.FeatureManager.getByName
import me.zeroeightsix.kami.feature.plugin.Plugin
import net.minecraft.command.CommandSource
import net.minecraft.text.LiteralText
import java.util.concurrent.CompletableFuture
import java.util.function.Function

class PluginArgumentType : ArgumentType<Plugin> {

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
        FeatureManager.plugins.getByName(str)?.let {
            return@let it
        }
        throw invalidPluginException.create(str)
    }

    override fun <S> listSuggestions(
        context: CommandContext<S>?,
        builder: SuggestionsBuilder?
    ): CompletableFuture<Suggestions?>? {
        return CommandSource.suggestMatching(
            FeatureManager.plugins.stream().map { obj: Plugin -> obj.name },
            builder
        )
    }

    override fun getExamples(): MutableCollection<String> {
        return examples
    }
}
