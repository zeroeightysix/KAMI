package me.zeroeightsix.kami.feature.command

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.StringReader.isQuotedStringStart
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import io.github.fablabsmc.fablabs.api.fiber.v1.tree.ConfigNode
import me.zeroeightsix.kami.feature.FullFeature
import me.zeroeightsix.kami.flattenedStream
import net.minecraft.server.command.CommandSource
import net.minecraft.text.LiteralText
import java.util.concurrent.CompletableFuture
import java.util.function.Function

class SettingArgumentType(
    dependantType: ArgumentType<FullFeature>,
    dependantArgument: String,
    shift: Int
) : DependantArgumentType<ConfigNode, FullFeature>(
    dependantType,
    dependantArgument,
    shift
) {

    @Throws(CommandSyntaxException::class)
    override fun parse(reader: StringReader): ConfigNode {
        val feature = findDependencyValue(reader)
        val string = when (isQuotedStringStart(reader.peek())) {
            true -> reader.readQuotedString()
            false -> reader.readUnquotedString()
        }
        val s = feature.config.flattenedStream()
            .filter {
                it.name.equals(string, ignoreCase = true)
            }.findAny()
        return if (s.isPresent) {
            s.get()
        } else {
            throw INVALID_SETTING_EXCEPTION.create(arrayOf(string, feature.name))
        }
    }

    override fun <S> listSuggestions(
        context: CommandContext<S>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions>? {
        val f = findDependencyValue(context, FullFeature::class.java)
        fun String.quoteIfNecessary(): String {
            if (contains(' ')) return "\"$this\""
            return this
        }
        return CommandSource.suggestMatching(f.config.items.stream().map { it.name.quoteIfNecessary() }, builder)
    }

    override fun getExamples(): Collection<String> {
        return EXAMPLES
    }

    companion object {
        private val EXAMPLES: Collection<String> = listOf("enabled", "speed")
        val INVALID_SETTING_EXCEPTION =
            DynamicCommandExceptionType(Function { `object`: Any ->
                LiteralText(
                    "Unknown setting '" + (`object` as Array<*>)[0] + "' for feature " + `object`[1]
                )
            })

        fun setting(
            dependentType: FullFeatureArgumentType,
            featureArgName: String,
            shift: Int
        ): SettingArgumentType {
            return SettingArgumentType(dependentType, featureArgName, shift)
        }
    }
}
