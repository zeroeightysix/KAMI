package me.zeroeightsix.kami.mimic

import net.minecraft.client.MinecraftClient
import net.minecraft.entity.Entity
import net.minecraft.fluid.FluidState
import net.minecraft.tag.FluidTags
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.BlockPos.PooledMutable
import net.minecraft.util.math.Box
import net.minecraft.util.math.MathHelper

interface TrajectoryMimic {

    var x: Double
    var y: Double
    var z: Double

    var landed: Boolean
    var entity: Entity?
    var block: BlockPos?

    fun tick()

    fun isInWater(box: Box): Boolean {
        val box = box.contract(0.001)
        val i = MathHelper.floor(box.minX)
        val j = MathHelper.ceil(box.maxX)
        val k = MathHelper.floor(box.minY)
        val l = MathHelper.ceil(box.maxY)
        val m = MathHelper.floor(box.minZ)
        val n = MathHelper.ceil(box.maxZ)
        val pooledMutable = PooledMutable.get()
        try {
            for (p in i until j) {
                for (q in k until l) {
                    for (r in m until n) {
                        pooledMutable.method_10113(p, q, r)
                        val fluidState: FluidState = MinecraftClient.getInstance().world.getFluidState(pooledMutable)
                        if (fluidState.matches(FluidTags.WATER)) {
                            val e =
                                (q.toFloat() + fluidState.getHeight(
                                    MinecraftClient.getInstance().world,
                                    pooledMutable
                                )).toDouble()
                            if (e >= box.minY) {
                                return true
                            }
                        }
                    }
                }
            }
        } catch (e: Throwable) {
        } finally {
            pooledMutable?.close()
        }

        return false
    }

}
