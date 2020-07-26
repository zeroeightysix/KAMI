package me.zeroeightsix.kami.mimic

import net.minecraft.client.MinecraftClient
import net.minecraft.entity.Entity
import net.minecraft.fluid.FluidState
import net.minecraft.tag.FluidTags
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.BlockPos.PooledMutable
import net.minecraft.util.math.Box
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d

interface TrajectoryMimic {

    var x: Double
    var y: Double
    var z: Double

    var yaw: Float
    var pitch: Float
    var prevYaw: Float
    var prevPitch: Float

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

    fun correctYawPitch(vec3d: Vec3d) {
        val f = MathHelper.sqrt(Entity.squaredHorizontalLength(vec3d))
        this.yaw = (MathHelper.atan2(vec3d.x, vec3d.z) * 57.2957763671875).toFloat()
        this.pitch = (MathHelper.atan2(vec3d.y, f.toDouble()) * 57.2957763671875).toFloat()
        this.prevYaw = this.yaw
        this.prevPitch = this.pitch
    }

    fun dropPitchAndYaw() {
        while (pitch - prevPitch < -180.0f) {
            prevPitch -= 360.0f
        }
        while (pitch - prevPitch >= 180.0f) {
            prevPitch += 360.0f
        }
        while (yaw - prevYaw < -180.0f) {
            prevYaw -= 360.0f
        }
        while (yaw - prevYaw >= 180.0f) {
            prevYaw += 360.0f
        }
        pitch = MathHelper.lerp(0.2f, prevPitch, pitch)
        yaw = MathHelper.lerp(0.2f, prevYaw, yaw)
    }

}
