package me.zeroeightsix.kami.feature.command

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import me.zeroeightsix.kami.setting.KamiConfig
import me.zeroeightsix.kami.util.Bind
import java.math.BigDecimal
import java.util.concurrent.CompletableFuture

class BindArgumentType private constructor() : ArgumentType<Bind> {
    companion object {
        fun bind(): BindArgumentType = BindArgumentType()

        val bindInterface = KamiConfig.typeMap[Bind::class.java]!!
    }

    override fun parse(reader: StringReader?): Bind =
        KamiConfig.bindType.toRuntimeType(bindInterface.valueFromString(reader!!.readUnquotedString()) as MutableMap<String, BigDecimal>?)

    override fun <S : Any?> listSuggestions(
        context: CommandContext<S>?,
        builder: SuggestionsBuilder?
    ): CompletableFuture<Suggestions> = bindInterface.listSuggestions(context!!, builder!!)
}
