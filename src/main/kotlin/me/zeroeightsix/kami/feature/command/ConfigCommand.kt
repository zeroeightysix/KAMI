package me.zeroeightsix.kami.feature.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import me.zeroeightsix.kami.setting.KamiConfig
import me.zeroeightsix.kami.util.text
import net.minecraft.command.CommandSource
import net.minecraft.text.LiteralText
import net.minecraft.util.Formatting.GOLD
import net.minecraft.util.Formatting.YELLOW
import java.nio.file.Paths
import java.util.function.Function

object ConfigCommand : Command() {
    private val FAILED_EXCEPTION =
        DynamicCommandExceptionType(
            Function { o: Any ->
                LiteralText(o.toString())
            }
        )

    override fun register(dispatcher: CommandDispatcher<CommandSource>) {
        dispatcher register rootLiteral("config") {
            literal("reload") {
                does {
                    try {
                        KamiConfig.loadConfiguration()
                    } catch (e: Exception) {
                        throw FAILED_EXCEPTION.create(e.message)
                    }
                    it replyWith text(GOLD, "Reloaded configuration!")
                    0
                }
            }
            literal("save") {
                does {
                    KamiConfig.saveConfiguration()
                    it replyWith text(GOLD, "Saved configuration!")
                    0
                }
            }
            literal("where") {
                does {
                    val path = Paths.get(KamiConfig.CONFIG_FILENAME)
                    it replyWith text {
                        +"The configuration file is at "(GOLD)
                        +path.toAbsolutePath().toString()(YELLOW)
                    }
                    0
                }
            }
        }
    }
}
