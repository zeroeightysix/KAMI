package me.zeroeightsix.kami.mimic

import me.zeroeightsix.kami.util.EntityUtil
import net.minecraft.block.BlockState
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World

class ThrowableMimic(val world: World, val shooter: LivingEntity, val type: EntityType<*>) : TrajectoryMimic {

    override var x = 0.0
    override var y = 0.0
    override var z = 0.0
    override var landed = false
    override var entity: Entity? = null
    override var block: BlockPos? = null

    lateinit var velocity: Vec3d
    lateinit var boundingBox: Box
    val dimensions = type.dimensions
    var yaw = 0f
    var pitch = 0f
    var prevYaw = 0f
    var prevPitch = 0f

    init {
        val pos = EntityUtil.getInterpolatedPos(shooter, MinecraftClient.getInstance().tickDelta)
        setPosition(pos.x, pos.y + shooter.standingEyeHeight.toDouble() - 0.10000000149011612, pos.z)
    }

    fun setPosition(x: Double, y: Double, z: Double) {
        this.x = x
        this.y = y
        this.z = z
        val f = dimensions.width / 2.0f
        val g = dimensions.height
        boundingBox = Box(x - f.toDouble(), y, z - f.toDouble(), x + f.toDouble(), y + g.toDouble(), z + f.toDouble())
    }

    fun setVelocity(
        x: Double,
        y: Double,
        z: Double,
        speed: Float
    ) {
        val vec3d = Vec3d(x, y, z).normalize().multiply(speed.toDouble())
        velocity = vec3d
        val f = MathHelper.sqrt(Entity.squaredHorizontalLength(vec3d))
        this.yaw = (MathHelper.atan2(vec3d.x, vec3d.z) * 57.2957763671875).toFloat()
        this.pitch = (MathHelper.atan2(vec3d.y, f.toDouble()) * 57.2957763671875).toFloat()
        this.prevYaw = this.yaw
        this.prevPitch = this.pitch
    }

    fun setProperties(
        user: Entity,
        pitch: Float,
        yaw: Float,
        g: Float
    ) {
        val i = -MathHelper.sin(yaw * 0.017453292f) * MathHelper.cos(pitch * 0.017453292f)
        val j = -MathHelper.sin(pitch * 0.017453292f)
        val k = MathHelper.cos(yaw * 0.017453292f) * MathHelper.cos(pitch * 0.017453292f)
        this.setVelocity(i.toDouble(), j.toDouble(), k.toDouble(), g)
//        val vec3d = user.velocity
//        velocity = velocity.add(vec3d.x, if (user.onGround) 0.0 else vec3d.y, vec3d.z)
    }

    override fun tick() {
        val box: Box = boundingBox.stretch(velocity).expand(1.0)
        val collisions =
            world.getEntities(null as Entity?, box) { entityx: Entity -> !entityx.isSpectator && entityx.collides() }
                .filterNot { it == shooter }

        if (collisions.isNotEmpty()) {
            landed = true
            entity = collisions.first()
            return
        }

        val blockPos = BlockPos(this.x, this.y, this.z)
        val blockState: BlockState = this.world.getBlockState(blockPos)
        if (!blockState.isAir) {
            val voxelShape = blockState.getCollisionShape(this.world, blockPos)
            if (!voxelShape.isEmpty) {
                val var6: Iterator<*> = voxelShape.boundingBoxes.iterator()
                while (var6.hasNext()) {
                    val box = var6.next() as Box
                    if (box.offset(blockPos).contains(Vec3d(this.x, this.y, this.z))) {
                        this.landed = true
                        this.block = blockPos
                        return
                    }
                }
            }
        }

        val vec3d: Vec3d = velocity
        x += vec3d.x
        y += vec3d.y
        z += vec3d.z
        val f = MathHelper.sqrt(Entity.squaredHorizontalLength(vec3d))
        yaw = (MathHelper.atan2(vec3d.x, vec3d.z) * 57.2957763671875).toFloat()

        pitch = (MathHelper.atan2(vec3d.y, f.toDouble()) * 57.2957763671875).toFloat()
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
        val slowdown = if (isInWater(boundingBox)) {
            0.8
        } else {
            0.99
        }
        velocity = vec3d.multiply(slowdown)

        velocity = Vec3d(velocity.x, velocity.y - 0.03, velocity.z)

        setPosition(x, y, z)

        landed = landed || y < 0
    }

}
