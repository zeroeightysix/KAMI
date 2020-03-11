package me.zeroeightsix.kami.setting.impl

import com.google.common.base.Converter
import com.google.gson.JsonElement
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import imgui.NUL
import imgui.dsl
import imgui.dsl.combo
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.setting.converter.EnumConverter
import net.minecraft.server.command.CommandSource
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.function.BiConsumer
import java.util.function.Predicate

/**
 * Created by 086 on 14/10/2018.
 */
class EnumSetting<T : Enum<*>?>(
    value: T,
    restriction: Predicate<T>?,
    consumer: BiConsumer<T, T>?,
    name: String?,
    visibilityPredicate: Predicate<T>?,
    clazz: Class<out Enum<*>>
) : Setting<T>(value, restriction, consumer, name, visibilityPredicate) {
    private val converter: EnumConverter = EnumConverter(clazz)
    val clazz: Class<out Enum<*>>
    private var selected = 0

    override fun converter(): Converter<T, JsonElement> {
        return converter as Converter<T, JsonElement>
    }

    override fun <S> listSuggestions(
        context: CommandContext<S>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        return CommandSource.suggestMatching(
            Arrays.stream(clazz.enumConstants).map { obj: Any -> obj.toString() },
            builder
        )
    }

    override fun drawSettings() {
        combo(name, ::selected, clazz.enumConstants.joinToString("$NUL") { it.name.toLowerCase().capitalize().replace('_', ' ') }) {
            setValue(clazz.enumConstants[selected] as T)
            selected = clazz.enumConstants.indexOf(value)
        }
    }

    init {
        this.clazz = clazz
    }
}