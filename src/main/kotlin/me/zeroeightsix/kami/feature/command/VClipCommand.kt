package me.zeroeightsix.kami.feature.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.FloatArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import me.zeroeightsix.kami.mc
import net.minecraft.server.command.CommandSource

object VClipCommand : Command() {
    override fun register(dispatcher: CommandDispatcher<CommandSource>) {
        dispatcher.register(
            LiteralArgumentBuilder.literal<CommandSource>("vclip")
                .then(RequiredArgumentBuilder.argument<CommandSource, Float>("height", FloatArgumentType.floatArg())
                    .executes { ctx ->
                        mc.player?.let {
                            it.updatePosition(
                                it.x,
                                it.y + ctx.getArgument("height", Float::class.java),
                                it.z
                            )
                        }
                        0
                    })

        )
    }
}
