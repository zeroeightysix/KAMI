package me.zeroeightsix.kami.feature.module

import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting
import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.TickEvent.Client.InGame
import me.zeroeightsix.kami.interpolatedPos
import me.zeroeightsix.kami.mixin.client.IMinecraftClient
import me.zeroeightsix.kami.util.Wrapper
import net.minecraft.block.*
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemStack
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.LookOnly
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import java.util.*

@Module.Info(name = "Scaffold", category = Module.Category.PLAYER)
object Scaffold : Module() {
    private val blackList = Arrays.asList(
        Blocks.ENDER_CHEST,
        Blocks.CHEST,
        Blocks.TRAPPED_CHEST
    )

    @Setting(name = "Ticks")
    private var future = 2

    private fun hasNeighbour(blockPos: BlockPos): Boolean {
        for (side in Direction.values()) {
            val neighbour = blockPos.offset(side)
            if (!Wrapper.getWorld().getBlockState(neighbour).material.isReplaceable) return true
        }
        return false
    }

    @EventHandler
    private val updateListener = Listener(EventHook<InGame> {
        val vec3d = mc.player?.interpolatedPos ?: return@EventHook
        var blockPos: BlockPos = BlockPos(vec3d).down()
        val belowBlockPos = blockPos.down()

        // check if block is already placed
        if (!mc.world!!.getBlockState(blockPos).material.isReplaceable) return@EventHook

        // search blocks in hotbar
        var newSlot = -1
        var i = 0
        while (i < 9) {

            // filter out non-block items
            val stack = mc.player!!.inventory.getStack(i)
            if (stack == ItemStack.EMPTY || stack.item !is BlockItem) {
                i++
                continue
            }
            val block = (stack.item as BlockItem).block
            if (blackList.contains(block) || block is BlockWithEntity) {
                i++
                continue
            }

            // filter out non-solid blocks
            if (!Block.getBlockFromItem(stack.item).defaultState
                    .isOpaque
            ) {
                i++
                continue
            }

            // don't use falling blocks if it'd fall
            if ((stack.item as BlockItem).block is FallingBlock) {
                if (Wrapper.getWorld().getBlockState(belowBlockPos).material.isReplaceable) {
                    i++
                    continue
                }
            }
            newSlot = i
            break
            i++
        }

        // check if any blocks were found
        if (newSlot == -1) return@EventHook

        // set slot
        val oldSlot = Wrapper.getPlayer().inventory.selectedSlot
        Wrapper.getPlayer().inventory.selectedSlot = newSlot

        // check if we don't have a block adjacent to blockpos
        if (!hasNeighbour(blockPos)) {
            // find air adjacent to blockpos that does have a block adjacent to it, let's fill this first as to form a bridge between the player and the original blockpos. necessary if the player is going diagonal.
            var broke = false
            for (side in Direction.values()) {
                val neighbour = blockPos.offset(side)
                if (hasNeighbour(neighbour)) {
                    blockPos = neighbour
                    broke = true
                    break
                }
            }
            if (!broke) {
                return@EventHook
            }
        }

        // place block
        placeBlockScaffold(blockPos)

        // reset slot
        Wrapper.getPlayer().inventory.selectedSlot = oldSlot
    })

    companion object {
        fun placeBlockScaffold(pos: BlockPos): Boolean {
            val eyesPos = Vec3d(
                Wrapper.getPlayer().x,
                Wrapper.getPlayer().y + Wrapper.getPlayer().getEyeHeight(Wrapper.getPlayer().pose),
                Wrapper.getPlayer().z
            )
            for (side in Direction.values()) {
                val neighbor = pos.offset(side)
                val side2 = side.opposite

                // check if side is visible (facing away from player)
                if (eyesPos.squaredDistanceTo(
                        Vec3d(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble()).add(0.5, 0.5, 0.5)
                    ) >= eyesPos
                        .squaredDistanceTo(
                            Vec3d(
                                neighbor.x.toDouble(), neighbor.y.toDouble(), neighbor.z.toDouble()
                            ).add(0.5, 0.5, 0.5)
                        )
                ) continue

                // check if neighbor can be right clicked
                if (!canBeClicked(neighbor)) continue
                val hitVec = Vec3d(
                    neighbor.x.toDouble(), neighbor.y.toDouble(), neighbor.z.toDouble()
                ).add(0.5, 0.5, 0.5)
                    .add(
                        Vec3d(
                            side2.vector.x.toDouble(), side2.vector.y.toDouble(), side2.vector.z.toDouble()
                        ).multiply(0.5)
                    )

                // check if hitVec is within range (4.25 blocks)
                if (eyesPos.squaredDistanceTo(hitVec) > 18.0625) continue

                // place block
                faceVectorPacketInstant(hitVec)
                processRightClickBlock(neighbor, side2, hitVec)
                Wrapper.getPlayer().swingHand(Hand.MAIN_HAND)
                (mc as IMinecraftClient).setItemUseCooldown(4)
                return true
            }
            return false
        }

        fun processRightClickBlock(
            pos: BlockPos?, side: Direction?,
            hitVec: Vec3d?
        ) {
            mc.interactionManager!!.interactBlock(
                Wrapper.getPlayer(),
                mc.world, Hand.MAIN_HAND, BlockHitResult(hitVec, side, pos, false)
            )
        }

        fun getState(pos: BlockPos?): BlockState {
            return Wrapper.getWorld().getBlockState(pos)
        }

        fun getBlock(pos: BlockPos?): Block {
            return getState(pos).block
        }

        fun canBeClicked(pos: BlockPos?): Boolean {
            //return getBlock(pos).canCollideCheck(getState(pos), false);
            return true // TODO
        }

        @JvmStatic
        fun faceVectorPacketInstant(vec: Vec3d) {
            val rotations = getNeededRotations2(vec)
            mc.networkHandler!!.sendPacket(
                LookOnly(
                    rotations[0],
                    rotations[1], Wrapper.getPlayer().isOnGround
                )
            )
        }

        private fun getNeededRotations2(vec: Vec3d): FloatArray {
            val eyesPos = eyesPos
            val diffX = vec.x - eyesPos.x
            val diffY = vec.y - eyesPos.y
            val diffZ = vec.z - eyesPos.z
            val diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ)
            val yaw = Math.toDegrees(Math.atan2(diffZ, diffX)).toFloat() - 90f
            val pitch = (-Math.toDegrees(Math.atan2(diffY, diffXZ))).toFloat()
            return floatArrayOf(
                Wrapper.getPlayer().yaw
                        + MathHelper.wrapDegrees(yaw - Wrapper.getPlayer().yaw),
                Wrapper.getPlayer().pitch + MathHelper
                    .wrapDegrees(pitch - Wrapper.getPlayer().pitch)
            )
        }

        val eyesPos: Vec3d
            get() = Vec3d(
                Wrapper.getPlayer().x,
                Wrapper.getPlayer().y + Wrapper.getPlayer().getEyeHeight(
                    mc.player!!.pose
                ),
                Wrapper.getPlayer().z
            )
    }
}
