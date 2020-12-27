package me.zeroeightsix.kami.util

import me.zeroeightsix.kami.feature.module.Module
import me.zeroeightsix.kami.feature.module.Scaffold.eyesPos
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.client.world.ClientWorld
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import kotlin.math.atan2
import kotlin.math.sqrt

object Viewblock {
    @JvmStatic
    fun getNeededRotations(player: ClientPlayerEntity, vec: Vec3d): FloatArray {
        val eyesPos = player.eyesPos
        val diffX = vec.x - eyesPos.x
        val diffY = vec.y - eyesPos.y
        val diffZ = vec.z - eyesPos.z
        val diffXZ = sqrt(diffX * diffX + diffZ * diffZ)
        val yaw = Math.toDegrees(atan2(diffZ, diffX)).toFloat() - 90f
        val pitch = (-Math.toDegrees(atan2(diffY, diffXZ))).toFloat()
        return floatArrayOf(
            Wrapper.getPlayer().yaw +
                MathHelper.wrapDegrees(yaw - Wrapper.getPlayer().yaw),
            Wrapper.getPlayer().pitch + MathHelper
                .wrapDegrees(pitch - Wrapper.getPlayer().pitch)
        )
    }

    @JvmStatic
    fun faceVectorPacketInstant(player: ClientPlayerEntity, vec: Vec3d, mc: MinecraftClient) {
        val rotations = getNeededRotations(player, vec)
        mc.networkHandler!!.sendPacket(
            PlayerMoveC2SPacket.LookOnly(
                rotations[0],
                rotations[1],
                Wrapper.getPlayer().isOnGround
            )
        )
    }

    @JvmStatic
    fun getIrreplaceableNeighbour(world: ClientWorld, blockPos: BlockPos?): Pair<BlockPos, Direction>? {
        if (blockPos == null) return blockPos
        for (side in Direction.values()) {
            val neighbour = blockPos.offset(side)
            if (world.getBlockState(neighbour)?.material?.isReplaceable == false) return neighbour to side.opposite
        }
        return null
    }
}
