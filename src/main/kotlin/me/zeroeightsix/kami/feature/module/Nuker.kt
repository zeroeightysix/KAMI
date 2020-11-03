package me.zeroeightsix.kami.feature.module

import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting
import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.TickEvent
import me.zeroeightsix.kami.kotlin
import me.zeroeightsix.kami.setting.SettingVisibility
import me.zeroeightsix.kami.util.asVec3d
import me.zeroeightsix.kami.util.singleVec
import net.minecraft.block.FluidBlock
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.client.world.ClientWorld
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import net.minecraft.world.RaycastContext

@Module.Info(
    name = "Nuker",
    category = Module.Category.PLAYER,
    description = "Destroys Blocks around the Player"
)
object Nuker : Module() {
    @Setting
    private var usePlayerRange = true

    @Setting
    @SettingVisibility.Method("shouldUseRangeConfig")
    private var hitRange: @Setting.Constrain.Range(min = 0.0, max = 8.0, step = 1.0) Double = 4.0

    @Setting
    private var throughBlocks = false

    @Setting(comment = "Select the best tool for the block being broken")
    private var selectTool = false

    @Setting(comment = "Only break blocks that can be instantly broken")
    private var onlyInstant = false

    private var progress = 0.0
    private var currentBlock: BlockPos? = null

    private val range
        get() =
            if (usePlayerRange) mc.interactionManager?.reachDistance?.toDouble() ?: hitRange
            else hitRange

    // This exists to use the range without having to recalculate `range` a lot.
    // Instead it is calculated once at the start of the tick event
    private var currentRange = range

    @Suppress("UNUSED")
    fun shouldUseRangeConfig() = !usePlayerRange

    @EventHandler
    private val updateListener = Listener<TickEvent.InGame>({
        currentRange = range
        instantMineBlocks(it.player, it.world)

        if (currentBlock == null || !validate(it.player, it.world, currentBlock!!))
            currentBlock = nextBlock(it.player, it.world)

        if (!it.player.isCreative && !onlyInstant) {
            val block = currentBlock ?: return@Listener
            val state = it.world.getBlockState(block)

            if (selectTool)
                AutoTool.equipBestTool(state)

            if (progress == 0.0)
                mine(block, true)

            progress += state.calcBlockBreakingDelta(it.player, it.world, block)
            if (progress >= 1.0) {
                mine(block, false)
                progress = 0.0
                currentBlock = null
            }
        }
    })

    private fun getBoxCorner(playerPos: Vec3d, negative: Boolean = false) =
        playerPos.add(singleVec(currentRange).run { if (negative) negate() else this })

    private fun getValidBlocks(player: ClientPlayerEntity, world: ClientWorld) =
        BlockPos.method_29715(Box(getBoxCorner(player.pos), getBoxCorner(player.pos, true)))
            .filter { validate(player, world, it) }

    /**
     * Mines a block using packets
     *
     * @param op if `true`, mining is started. Otherwise, it is stopped.
     */
    private fun mine(block: BlockPos, op: Boolean) {
        mc.networkHandler?.sendPacket(
            PlayerActionC2SPacket(
                if (op) PlayerActionC2SPacket.Action.START_DESTROY_BLOCK
                else PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK,
                block,
                Direction.DOWN
            )
        )
    }

    private fun nextBlock(player: ClientPlayerEntity, world: ClientWorld) =
        getValidBlocks(player, world).findFirst().kotlin

    private fun instantMineBlocks(player: ClientPlayerEntity, world: ClientWorld) {
        getValidBlocks(player, world)
            .filter { canInstaMine(player, world, it) }
            .forEach { mine(it, true) }
    }

    private fun canInstaMine(player: ClientPlayerEntity, world: ClientWorld, block: BlockPos) =
        player.isCreative || world.getBlockState(block).calcBlockBreakingDelta(player, world, block) >= 1

    private fun validate(player: ClientPlayerEntity, world: ClientWorld, block: BlockPos): Boolean {
        val state = world.getBlockState(block)

        val throughBlockCheck =
            throughBlocks ||
                world.raycast(
                    RaycastContext(
                        Vec3d(player.pos.x, player.eyeY, player.pos.z),
                        block.asVec3d.add(singleVec(0.5)),
                        RaycastContext.ShapeType.COLLIDER,
                        RaycastContext.FluidHandling.NONE,
                        player
                    )
                ).blockPos == block

        if (!throughBlockCheck) return false

        return !state.isAir &&
            state.block !is FluidBlock &&
            block.isWithinDistance(
                player.pos,
                currentRange
            ) &&
            (player.isCreative || state.getHardness(world, block) >= 0)
    }
}
