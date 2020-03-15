package me.zeroeightsix.kami.event.events

import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction

class PlayerAttackBlockEvent(val position: BlockPos, val facing: Direction)