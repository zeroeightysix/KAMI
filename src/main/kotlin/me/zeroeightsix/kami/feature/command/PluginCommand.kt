package me.zeroeightsix.kami.feature.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import me.zeroeightsix.kami.feature.plugin.Plugin
import me.zeroeightsix.kami.util.text
import net.minecraft.server.command.CommandSource
import net.minecraft.text.LiteralText
import net.minecraft.util.Formatting.GOLD
import net.minecraft.util.Formatting.YELLOW

object PluginCommand : Command() {

    private val failedException = DynamicCommandExceptionType { o: Any -> LiteralText(o.toString()) }

    override fun register(dispatcher: CommandDispatcher<CommandSource>) {
        dispatcher register rootLiteral("plugins") {
            literal("enable") {
                argument("plugin", PluginArgumentType.plugin()) {
                    does { ctx ->
                        toggle(
                            "enable",
                            ctx.source as KamiCommandSource,
                            "plugin" from ctx,
                            true
                        )
                        0
                    }
                }
            }
            literal("disable") {
                argument("plugin", PluginArgumentType.plugin()) {
                    does { ctx ->
                        toggle(
                            "disable",
                            ctx.source as KamiCommandSource,
                            "plugin" from ctx,
                            false
                        )
                        0
                    }
                }
            }
        }
    }

    private fun toggle(word: String, source: KamiCommandSource, plugin: Plugin, enable: Boolean) {
        if (plugin.enabled != enable) {
            plugin.enabled = enable
            source replyWith text(GOLD) {
                +"${word.capitalize()}d plugin "
                +plugin.name(YELLOW)
            }
        } else
            source replyWith text(GOLD) {
                +plugin.name(YELLOW)
                +"is already ${word}d."
            }
    }

}
