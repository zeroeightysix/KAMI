package me.zeroeightsix.kami.mimic

import net.minecraft.entity.Entity
import net.minecraft.util.math.BlockPos

interface TrajectoryMimic {

    var x: Double
    var y: Double
    var z: Double

    var landed: Boolean
    var entity: Entity?
    var block: BlockPos?

    fun tick()

}
