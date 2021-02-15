package me.zeroeightsix.kami.feature.command

import com.mojang.brigadier.CommandDispatcher
import me.zeroeightsix.kami.mc
import net.minecraft.command.CommandSource
import net.minecraft.util.math.Vec3d

object HClipCommand : Command() {
    override fun register(dispatcher: CommandDispatcher<CommandSource>) {
        dispatcher register rootLiteral("hclip") {
            float("blocks") {
                does { ctx ->
                    mc.player?.let {
                        val direction = Vec3d.fromPolar(0f, it.yaw)
                        it.updatePosition(
                            it.x + direction.x * ("blocks".from<Float, CommandSource>(ctx)),
                            it.y,
                            it.z + direction.z * ("blocks".from<Float, CommandSource>(ctx))
                        )
                    }
                    0
                }
            }
        }
    }
}