package me.zeroeightsix.kami.feature.module

import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting
import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.TickEvent.InGame
import me.zeroeightsix.kami.getInterpolatedPos
import me.zeroeightsix.kami.mixin.client.IMinecraftClient
import me.zeroeightsix.kami.mixin.extend.itemUseCooldown
import me.zeroeightsix.kami.util.Viewblock
import me.zeroeightsix.kami.util.Viewblock.getIrreplaceableNeighbour
import me.zeroeightsix.kami.util.Wrapper
import me.zeroeightsix.kami.util.asVec
import me.zeroeightsix.kami.util.asVec3d
import me.zeroeightsix.kami.util.div
import me.zeroeightsix.kami.util.plus
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.BlockWithEntity
import net.minecraft.block.Blocks
import net.minecraft.block.FallingBlock
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.client.world.ClientWorld
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemStack
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d

@Module.Info(name = "Scaffold", category = Module.Category.PLAYER)
object Scaffold : Module() {
    @Setting(comment = "Allow placing blocks without a supporting block")
    private var midAir: Boolean = false

    private val blackList = listOf(
        Blocks.ENDER_CHEST,
        Blocks.CHEST,
        Blocks.TRAPPED_CHEST
    )

    @EventHandler
    private val updateListener = Listener<InGame>({ it ->
        val player = it.player
        val world = it.world

        val vec3d = player.getInterpolatedPos()
        val blockPos: BlockPos = BlockPos(vec3d).down()
        val belowBlockPos = blockPos.down()

        // check if block is already placed
        if (!world.getBlockState(blockPos).material.isReplaceable) return@Listener

        // search blocks in hotbar
        var newSlot = -1
        var i = 0
        while (i < 9) {

            // filter out non-block items
            val stack = player.inventory.getStack(i)
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
        }

        // check if any blocks were found
        if (newSlot == -1) return@Listener

        // set slot
        val oldSlot = Wrapper.getPlayer().inventory.selectedSlot
        Wrapper.getPlayer().inventory.selectedSlot = newSlot

        // If midAir is on, don't search for a neighbour block, but place the block in mid air
        if (midAir) {
            placeBlockScaffold(player, world, blockPos, Direction.DOWN)
        } else {
            // check if we don't have a block adjacent to blockpos
            getIrreplaceableNeighbour(world, blockPos).let {
                if (it == null) {
                    for (side in Direction.values()) {
                        return@let getIrreplaceableNeighbour(world, blockPos.offset(side)) ?: continue
                    }
                }
                it
            }?.let { (solid, side) ->
                // place block
                placeBlockScaffold(player, world, solid, side)
            }
        }

        // reset slot
        Wrapper.getPlayer().inventory.selectedSlot = oldSlot
    })

    private fun placeBlockScaffold(player: ClientPlayerEntity, world: ClientWorld, solid: BlockPos, side: Direction) {
//        faceVectorPacketInstant(hitVec) // TODO: some util that manages look packets. Now the player will rapidly look up and down which is silly
        mc.interactionManager?.interactBlock(
            player,
            world,
            Hand.MAIN_HAND,
            BlockHitResult(solid.asVec + 0.5 + side.vector.asVec3d / 2.0, side, solid, false)
        )
        Wrapper.getPlayer().swingHand(Hand.MAIN_HAND)
        mc.itemUseCooldown = 4
    }

    fun getState(pos: BlockPos?): BlockState {
        return Wrapper.getWorld().getBlockState(pos)
    }

    fun getBlock(pos: BlockPos?): Block {
        return getState(pos).block
    }

    val ClientPlayerEntity.eyesPos: Vec3d
        get() = Vec3d(x, y + getEyeHeight(pose), z)
}
