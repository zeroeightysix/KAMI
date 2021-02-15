package me.zeroeightsix.kami.feature.command

import com.mojang.brigadier.CommandDispatcher
import me.zeroeightsix.kami.mc
import net.minecraft.command.CommandSource
import net.minecraft.util.math.Vec3d

object HClipCommand : Command() {
    override fun register(dispatcher: CommandDispatcher<CommandSource>) {
        dispatcher register rootLiteral("hclip") {
            float("distance") {
                does { ctx ->
                    mc.player?.let {
                        val direction = Vec3d.fromPolar(0f, it.yaw)
                        val distance: Float = "distance" from ctx
                        it.updatePosition(
                            it.x + direction.x * distance,
                            it.y,
                            it.z + direction.z * distance
                        )
                    }
                    0
                }
            }
        }
    }
}