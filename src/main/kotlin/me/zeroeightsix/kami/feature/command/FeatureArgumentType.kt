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
import me.zeroeightsix.kami.feature.HasBind
import net.minecraft.server.command.CommandSource
import net.minecraft.text.LiteralText
import java.util.NoSuchElementException
import java.util.concurrent.CompletableFuture

class FeatureArgumentType<T>(val clazz: Class<T>) : ArgumentType<T> {
    @Throws(CommandSyntaxException::class)
    override fun parse(reader: StringReader): T {
        val string = reader.readUnquotedString()
        try {
            return clazz.cast(
                features.filter { clazz.isInstance(it) }
                    .first { it.name.equals(string, ignoreCase = true) }
            )
        } catch (e: NoSuchElementException) {
            throw INVALID_MODULE_EXCEPTION.create(string)
        }
    }

    override fun <S> listSuggestions(
        context: CommandContext<S>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        return CommandSource.suggestMatching(
            features.filter { clazz.isInstance(it) }.map { it.name },
            builder
        )
    }

    override fun getExamples(): Collection<String> {
        return EXAMPLES
    }

    companion object {
        private val EXAMPLES: Collection<String> = listOf("Aura", "CameraClip", "Flight")
        val INVALID_MODULE_EXCEPTION =
            DynamicCommandExceptionType { obj ->
                LiteralText("Unknown feature '$obj'")
            }

        fun fullFeature(): FeatureArgumentType<FullFeature> = FeatureArgumentType(FullFeature::class.java)
        fun boundFeature() = FeatureArgumentType(HasBind::class.java)
    }
}
