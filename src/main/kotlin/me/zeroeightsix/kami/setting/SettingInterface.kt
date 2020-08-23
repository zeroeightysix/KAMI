package me.zeroeightsix.kami.setting

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.SerializableType
import io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.StringSerializableType
import io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.derived.ConfigType
import io.github.fablabsmc.fablabs.api.fiber.v1.tree.ConfigLeaf
import io.github.fablabsmc.fablabs.impl.fiber.annotation.BackedConfigLeaf
import me.zeroeightsix.kami.mixin.client.IBackedConfigLeaf
import me.zeroeightsix.kami.mixin.duck.HasSettingInterface
import me.zeroeightsix.kami.then
import java.util.concurrent.CompletableFuture

interface SettingInterface<R> {

    val type: String

    fun valueToString(value: R): String?
    fun valueFromString(str: String): R?

    /**
     * @return the modified value, if any
     */
    fun displayImGui(name: String, value: R): R?
    fun listSuggestions(context: CommandContext<*>, builder: SuggestionsBuilder): CompletableFuture<Suggestions> =
        builder.buildFuture()

    fun canFromString(str: String) =
        try {
            valueFromString(str)
            true
        } catch (e: Exception) {
            false
        }

}

inline fun <reified R, S, T : SerializableType<S>> ConfigType<R, S, T>.extend(
    crossinline valueToString: (R) -> String?,
    crossinline valueFromString: (String) -> R?,
    crossinline displayImGui: (String, R) -> R?,
    crossinline listSuggestions: (CommandContext<*>, SuggestionsBuilder) -> CompletableFuture<Suggestions> =
        { _, b -> b.buildFuture() },
    type: String = R::class.java.simpleName.toLowerCase()
): ConfigType<R, S, T> {
    (this as HasSettingInterface<R>).settingInterface = object : SettingInterface<R> {
        override val type: String = type

        override fun valueToString(value: R) = valueToString(value)
        override fun valueFromString(str: String) = valueFromString(str)
        override fun displayImGui(name: String, value: R) = displayImGui(name, value)
        override fun listSuggestions(
            context: CommandContext<*>,
            builder: SuggestionsBuilder
        ) = listSuggestions(context, builder)
    }
    return this
}

inline fun <reified R> ConfigType<R, String, StringSerializableType>.extend(
    crossinline displayImGui: (String, R) -> R?,
    crossinline listSuggestions: (CommandContext<*>, SuggestionsBuilder) -> CompletableFuture<Suggestions> =
        { _, b -> b.buildFuture() },
    type: String = R::class.java.simpleName.toLowerCase()
): ConfigType<R, String, StringSerializableType> {
    val toString = this::toPlatformType
    val fromString = this::toRuntimeType
    return this.extend(toString, fromString, displayImGui, listSuggestions, type)
}

fun <R, T> ConfigLeaf<T>.getRuntimeConfigType() =
    (this is BackedConfigLeaf<*, *>).then {
        (this as? BackedConfigLeaf<R, T>)?.runtimeConfigType
    }

fun <R, T> ConfigLeaf<T>.getInterface() =
    (getRuntimeConfigType<R, T>() as? HasSettingInterface<R>)?.settingInterface

/**
 * Get the [interface](getInterface) without the type parameter `R` (relying on `Any` instead)
 */
fun <T> ConfigLeaf<T>.getAnyInterface() = this.getInterface<Any, T>()

fun <T> ConfigLeaf<T>.getAnyRuntimeConfigType() = this.getRuntimeConfigType<Any, T>()

val <R, S> ConfigType<R, S, *>.settingInterface: SettingInterface<R>?
    get() = (this as? HasSettingInterface<R>)?.settingInterface

val <R, S> BackedConfigLeaf<R, S>.runtimeConfigType
    get() = (this as? IBackedConfigLeaf<R, S>)?.type
