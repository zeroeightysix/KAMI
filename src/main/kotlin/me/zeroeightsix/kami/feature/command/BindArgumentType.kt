package me.zeroeightsix.kami.feature.command

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import me.zeroeightsix.kami.setting.KamiConfig
import me.zeroeightsix.kami.setting.settingInterface
import me.zeroeightsix.kami.util.Bind
import java.util.concurrent.CompletableFuture

class BindArgumentType private constructor() : ArgumentType<Bind> {
    companion object {
        fun bind(): BindArgumentType = BindArgumentType()
    }

    override fun parse(reader: StringReader?): Bind? =
        KamiConfig.bindType.settingInterface?.valueFromString(reader!!.readUnquotedString())

    override fun <S : Any?> listSuggestions(
        context: CommandContext<S>,
        builder: SuggestionsBuilder,
    ): CompletableFuture<Suggestions> =
        KamiConfig.bindType.settingInterface?.listSuggestions(context, builder) ?: builder.buildFuture()
}
