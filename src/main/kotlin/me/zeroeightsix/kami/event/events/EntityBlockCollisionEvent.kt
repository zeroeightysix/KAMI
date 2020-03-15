package me.zeroeightsix.kami.event.events

import me.zeroeightsix.kami.event.KamiEvent
import net.minecraft.block.BlockState
import net.minecraft.entity.Entity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class EntityBlockCollisionEvent(
    val world: World,
    val state: BlockState,
    val pos: BlockPos,
    val entity: Entity
) : KamiEvent()