package me.zeroeightsix.kami.feature.command

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import me.zeroeightsix.kami.feature.FeatureManager.features
import me.zeroeightsix.kami.feature.FullFeature
import me.zeroeightsix.kami.feature.module.Module
import net.minecraft.server.command.CommandSource
import net.minecraft.text.LiteralText
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.function.Function

class ModuleArgumentType : ArgumentType<Module> {
    @Throws(CommandSyntaxException::class)
    override fun parse(reader: StringReader): Module {
        val string = reader.readUnquotedString()
        try {
            return features.filterIsInstance<Module>().first { it.name.value == string }
        } catch (e: NoSuchElementException) {
            throw INVALID_MODULE_EXCEPTION.create(string)
        }
    }

    override fun <S> listSuggestions(
        context: CommandContext<S>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        return CommandSource.suggestMatching(
            features.filterIsInstance<FullFeature>().map { it.name.value }, builder
        )
    }

    override fun getExamples(): Collection<String> {
        return EXAMPLES
    }

    companion object {
        private val EXAMPLES: Collection<String> = listOf("Aura", "CameraClip", "Flight")
        val INVALID_MODULE_EXCEPTION =
            DynamicCommandExceptionType(Function { `object`: Any ->
                LiteralText("Unknown module '$`object`'")
            })

        fun module(): ModuleArgumentType {
            return ModuleArgumentType()
        }
    }
}