package me.zeroeightsix.kami.feature.module

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.TickEvent.Client.InGame
import me.zeroeightsix.kami.getInterpolatedPos
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

    private fun getIrreplaceableNeighbour(blockPos: BlockPos): Pair<BlockPos, Direction>? {
        for (side in Direction.values()) {
            val neighbour = blockPos.offset(side)
            if (mc.world?.getBlockState(neighbour)?.material?.isReplaceable == false) return neighbour to side.opposite
        }
        return null
    }

    @EventHandler
    private val updateListener = Listener<InGame>({
        val vec3d = mc.player?.getInterpolatedPos() ?: return@Listener
        var blockPos: BlockPos = BlockPos(vec3d).down()
        val belowBlockPos = blockPos.down()

        // check if block is already placed
        if (!mc.world!!.getBlockState(blockPos).material.isReplaceable) return@Listener

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
        if (newSlot == -1) return@Listener

        // set slot
        val oldSlot = Wrapper.getPlayer().inventory.selectedSlot
        Wrapper.getPlayer().inventory.selectedSlot = newSlot

        // check if we don't have a block adjacent to blockpos
        getIrreplaceableNeighbour(blockPos).let {
            if (it == null) {
                for (side in Direction.values()) {
                    return@let getIrreplaceableNeighbour(blockPos.offset(side)) ?: continue
                }
            }
            it
        }?.let { (solid, side) ->
            // place block
            placeBlockScaffold(solid, side)
        }

        // reset slot
        Wrapper.getPlayer().inventory.selectedSlot = oldSlot
    })

    fun placeBlockScaffold(solid: BlockPos, side: Direction) {
//        faceVectorPacketInstant(hitVec) // TODO: some util that manages look packets. Now the player will rapidly look up and down which is silly
        mc.interactionManager?.interactBlock(
            mc.player,
            mc.world,
            Hand.MAIN_HAND,
            BlockHitResult(null, side, solid, false)
        )
        Wrapper.getPlayer().swingHand(Hand.MAIN_HAND)
        (mc as IMinecraftClient).setItemUseCooldown(4)
    }

    fun getState(pos: BlockPos?): BlockState {
        return Wrapper.getWorld().getBlockState(pos)
    }

    fun getBlock(pos: BlockPos?): Block {
        return getState(pos).block
    }

    fun canBeClicked(pos: BlockPos?): Boolean {
        return mc.world?.canPlace(getState(pos), pos, ShapeContext.absent()) ?: false
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
