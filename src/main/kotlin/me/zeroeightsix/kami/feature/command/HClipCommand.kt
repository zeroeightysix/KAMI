package me.zeroeightsix.kami.feature.command

import com.mojang.brigadier.CommandDispatcher
import me.zeroeightsix.kami.mc
import net.minecraft.command.CommandSource
import net.minecraft.util.math.Direction

object HClipCommand : Command() {
    override fun register(dispatcher: CommandDispatcher<CommandSource>) {
        dispatcher register rootLiteral("hclip") {
            float("blocks") {
                does { ctx ->
                    mc.player?.let {
                        when (it.horizontalFacing) {
                            Direction.NORTH -> {
                                it.updatePosition(
                                    it.x,
                                    it.y,
                                    it.z - ("blocks".from<Float, CommandSource>(ctx)))
                            }
                            Direction.EAST -> {
                                it.updatePosition(
                                    it.x + ("blocks".from<Float, CommandSource>(ctx)),
                                    it.y,
                                    it.z)
                            }
                            Direction.SOUTH -> {
                                it.updatePosition(
                                    it.x,
                                    it.y,
                                    it.z + ("blocks".from<Float, CommandSource>(ctx)))
                            }
                            else -> {
                                it.updatePosition(
                                    it.x - ("blocks".from<Float, CommandSource>(ctx)),
                                    it.y,
                                    it.z)
                            }
                        }
                    }
                    0
                }
            }
        }
    }
}