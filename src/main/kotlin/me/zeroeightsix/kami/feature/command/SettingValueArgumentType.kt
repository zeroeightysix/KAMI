package me.zeroeightsix.kami.feature.command

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import io.github.fablabsmc.fablabs.api.fiber.v1.FiberId
import io.github.fablabsmc.fablabs.api.fiber.v1.tree.ConfigLeaf
import me.zeroeightsix.kami.setting.SettingAnnotationProcessor
import me.zeroeightsix.kami.setting.SettingInterface
import net.minecraft.text.LiteralText
import java.util.concurrent.CompletableFuture
import java.util.function.Function

class SettingValueArgumentType(
    dependantType: ArgumentType<ConfigLeaf<*>>,
    dependantArgument: String,
    shift: Int
) : DependantArgumentType<String, ConfigLeaf<*>>(
    dependantType,
    dependantArgument,
    shift
) {
    @Throws(CommandSyntaxException::class)
    override fun parse(reader: StringReader): String {
        val setting = findDependencyValue(reader)
        val string = reader.readUnquotedString()
        return if (setting.getInterface().canFromString(string)) {
            string
        } else {
            throw INVALID_VALUE_EXCEPTION.create(
                arrayOf<Any>(
                    string,
                    setting.name
                )
            )
        }
    }

    override fun <S> listSuggestions(
        context: CommandContext<S>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        return findDependencyValue(context, ConfigLeaf::class.java).getInterface().listSuggestions(
            context,
            builder
        )
    }

    companion object {
        val INVALID_VALUE_EXCEPTION =
            DynamicCommandExceptionType(Function { `object`: Any ->
                LiteralText(
                    "Invalid value '" + (`object` as Array<Any>)[0] + "' for property '" + `object`[1] + "'"
                )
            })

        fun value(
            dependantType: ArgumentType<ConfigLeaf<*>>,
            dependantArgument: String,
            shift: Int
        ): SettingValueArgumentType {
            return SettingValueArgumentType(dependantType, dependantArgument, shift)
        }
    }
}

fun <T> ConfigLeaf<T>.getInterface(): SettingInterface<T> =
    this.getAttributeValue(FiberId("kami", "setting_interface"), SettingAnnotationProcessor.INTERFACE_TYPE).get() as SettingInterface<T>