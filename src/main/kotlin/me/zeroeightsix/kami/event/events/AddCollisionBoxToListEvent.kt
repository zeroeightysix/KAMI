package me.zeroeightsix.kami.event.events

import me.zeroeightsix.kami.event.KamiEvent
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.entity.Entity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.world.World

/**
 * Created by 086 on 11/12/2017.
 */
class AddCollisionBoxToListEvent(
    val block: Block,
    val state: BlockState,
    val world: World,
    val pos: BlockPos,
    val entityBox: Box,
    val collidingBoxes: List<Box>,
    val entity: Entity,
    val isBool: Boolean
) : KamiEvent()