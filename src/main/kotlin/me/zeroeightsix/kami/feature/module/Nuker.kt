package me.zeroeightsix.kami.feature.module

import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting
import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.TickEvent
import me.zeroeightsix.kami.util.asVec3d
import me.zeroeightsix.kami.util.single_vec
import net.minecraft.block.CommandBlock
import net.minecraft.block.FluidBlock
import net.minecraft.block.JigsawBlock
import net.minecraft.block.StructureBlock
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.client.world.ClientWorld
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import net.minecraft.world.RaycastContext

@Module.Info(name = "Nuker", category = Module.Category.PLAYER, description = "Destroys Blocks around the Player")
object Nuker : Module() {
    @Setting(name = "Hit Range")
    private var hitRange: @Setting.Constrain.Range(min = 0.0, max = 8.0, step = 1.0) Double = 4.0

    @Setting(name = "Through Blocks")
    private var throughBlocks = false

    @Setting(name = "Select Tool", comment = "select the best tool for the block being broken")
    private var selectTool = false

    @EventHandler
    private val updateListener = Listener<TickEvent.InGame>({
        instantMineBlocks(it.player, it.world)

        if (currentBlock == null) {
            nextBlock(it.player, it.world)
        }

        if (!it.player.isCreative && currentBlock != null) {
            val state = it.world.getBlockState(currentBlock!!)

            if (selectTool) {
                AutoTool.equipBestTool(state)
            }

            if (progress == 0.0) {
                startMine(currentBlock!!)
            }

            progress += state.calcBlockBreakingDelta(it.player, it.world, currentBlock)
            if (progress >= 1.0) {
                stopMine(currentBlock!!)
                progress = 0.0
                currentBlock = null
            }
        }
    })

    private var progress = 0.0
    private var currentBlock: BlockPos? = null

    private fun getBoxCorner(playerPos: Vec3d, negative: Boolean = false) =
        playerPos.add(Vec3d(hitRange, hitRange, hitRange).run { if (negative) negate() else this })

    private fun startMine(blockPos: BlockPos) {
        mc.networkHandler?.sendPacket(
            PlayerActionC2SPacket(
                PlayerActionC2SPacket.Action.START_DESTROY_BLOCK,
                blockPos,
                Direction.DOWN
            )
        )
    }

    private fun stopMine(blockPos: BlockPos) {
        mc.networkHandler?.sendPacket(
            PlayerActionC2SPacket(
                PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK,
                blockPos,
                Direction.DOWN
            )
        )
    }

    private fun nextBlock(player: ClientPlayerEntity, world: ClientWorld) {
        for (block in BlockPos.method_29715(Box(getBoxCorner(player.pos), getBoxCorner(player.pos, true)))) {
            if (validate(player, world, block)) {
                currentBlock = block
                break
            }
        }
    }

    private fun instantMineBlocks(player: ClientPlayerEntity, world: ClientWorld) {
        for (block in BlockPos.method_29715(Box(getBoxCorner(player.pos), getBoxCorner(player.pos, true)))) {
            if (validate(player, world, block) && canInstaMine(player, world, block)) {
                startMine(block)
            }
        }
    }

    private fun canInstaMine(player: ClientPlayerEntity, world: ClientWorld, block: BlockPos) =
        player.isCreative || world.getBlockState(block).calcBlockBreakingDelta(player, world, block) >= 1

    private fun validate(player: ClientPlayerEntity, world: ClientWorld, block: BlockPos): Boolean {
        val state = world.getBlockState(block)

        val throughBlockCheck = throughBlocks ||
            world.raycast(
                RaycastContext(
                    Vec3d(player.pos.x, player.eyeY, player.pos.z),
                    block.asVec3d.add(single_vec(0.5)),
                    RaycastContext.ShapeType.COLLIDER,
                    RaycastContext.FluidHandling.NONE,
                    player
                )
            ).blockPos == block

        val blockCheck = !state.isAir &&
            state.block !is FluidBlock &&
            block.isWithinDistance(
                player.pos,
                hitRange
            ) &&
            // this stupid way of checking for unbreakable blocks was not my idea
            (player.isCreative || !(block is CommandBlock || block is StructureBlock || block is JigsawBlock))

        return blockCheck && throughBlockCheck
    }
}
