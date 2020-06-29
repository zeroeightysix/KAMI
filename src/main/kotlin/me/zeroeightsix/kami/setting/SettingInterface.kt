package me.zeroeightsix.kami.setting

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import imgui.ImGui
import io.github.fablabsmc.fablabs.api.fiber.v1.tree.ConfigLeaf
import net.minecraft.server.command.CommandSource
import net.minecraft.util.Identifier
import net.minecraft.util.registry.SimpleRegistry
import java.lang.RuntimeException
import java.util.concurrent.CompletableFuture

interface SettingInterface<T> {

    val id: Identifier

    fun displayTypeAndValue(leaf: ConfigLeaf<T>) : Pair<String, String>
    fun valueFromString(str: String): T
    fun displayImGui(leaf: ConfigLeaf<T>)
    fun listSuggestions(context: CommandContext<*>, builder: SuggestionsBuilder): CompletableFuture<Suggestions>

    fun canFromString(str: String): Boolean {
        return try {
            valueFromString(str)
            true
        } catch (e: Exception) {
            false
        }
    }

    object Default : SettingInterface<Any> {
        override val id = Identifier("kami:default_displayer")

        override fun displayTypeAndValue(leaf: ConfigLeaf<Any>): Pair<String, String> = Pair("unknown", "unknown")

        override fun displayImGui(leaf: ConfigLeaf<Any>) {
            with (ImGui) {
                text("This setting can't be edited yet. Oops.")
            }
        }

        override fun valueFromString(str: String): Any {
            throw RuntimeException("This setting can't be set from a command.")
        }

        override fun listSuggestions(
            context: CommandContext<*>,
            builder: SuggestionsBuilder
        ): CompletableFuture<Suggestions> {
            return builder.buildFuture() // Suggest nothing
        }
    }

    object Registry : SimpleRegistry<SettingInterface<*>>() {
        init {
            add(Default.id, Default)
        }
    }

}