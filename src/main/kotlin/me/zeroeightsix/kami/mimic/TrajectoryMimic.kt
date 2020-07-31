package me.zeroeightsix.kami.mimic

import me.zeroeightsix.kami.mc
import net.minecraft.entity.Entity
import net.minecraft.fluid.FluidState
import net.minecraft.tag.FluidTags
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.BlockPos.PooledMutable
import net.minecraft.util.math.Box
import net.minecraft.util.math.Direction
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import net.minecraft.world.RayTraceContext
import net.minecraft.world.World

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
    var face: Direction?
    var hit: Vec3d?

    var diverged: Double

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
                        val fluidState: FluidState = mc.world.getFluidState(pooledMutable)
                        if (fluidState.matches(FluidTags.WATER)) {
                            val e =
                                (q.toFloat() + fluidState.getHeight(
                                    mc.world,
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

    fun checkCollision(velocity: Vec3d, shooter: Entity, world: World): Boolean {
        val here = Vec3d(this.x, this.y, this.z)
        val next = here.add(velocity)
        val traceContext = RayTraceContext(
            here,
            next,
            RayTraceContext.ShapeType.COLLIDER,
            RayTraceContext.FluidHandling.NONE,
            shooter
        )
        val trace = world.rayTrace(traceContext)

        if (trace.type != HitResult.Type.MISS) {
            face = trace.side
            hit = trace.pos
            this.landed = true
            return true
        }
        return false
    }

}
