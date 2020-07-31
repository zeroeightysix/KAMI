package me.zeroeightsix.kami.mimic

import me.zeroeightsix.kami.mc
import me.zeroeightsix.kami.util.EntityUtil
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.util.math.Box
import net.minecraft.util.math.Direction
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World

class ProjectileMimic(
    val world: World,
    val shooter: LivingEntity,
    val type: EntityType<*>,
    private val drag: Double,
    private val _divergence: Double
) :
    TrajectoryMimic {

    override var x = 0.0
    override var y = 0.0
    override var z = 0.0
    override var landed = false
    override var entity: Entity? = null
    override var yaw = 0f
    override var pitch = 0f
    override var prevYaw = 0f
    override var prevPitch = 0f
    override var diverged = 0.0
    override var face: Direction? = null
    override var hit: Vec3d? = null

    private lateinit var velocity: Vec3d
    private lateinit var boundingBox: Box
    private val dimensions = type.dimensions
    private var divergence: Double = 0.0

    init {
        val pos = EntityUtil.getInterpolatedPos(shooter, mc.tickDelta)
        setPosition(pos.x, pos.y + shooter.standingEyeHeight - 0.10000000149011612, pos.z)
    }

    override fun tick() {
        var vec3d = velocity

        if (this.prevPitch == 0.0f && this.prevYaw == 0.0f) {
            correctYawPitch(vec3d)
        }

        if (checkCollision(velocity, shooter, world)) return

        dropPitchAndYaw()

        vec3d = velocity
        val d = vec3d.x
        val e = vec3d.y
        val g = vec3d.z

        x += d
        y += e
        z += g

        diverged += velocity.length() * divergence * 0.007499999832361937

        val h = MathHelper.sqrt(Entity.squaredHorizontalLength(vec3d))
        yaw = (MathHelper.atan2(d, g) * 57.2957763671875).toFloat()
        pitch = (MathHelper.atan2(e, h.toDouble()) * 57.2957763671875).toFloat()

        dropPitchAndYaw()

        val slowdown = if (isInWater(boundingBox)) {
            drag
        } else {
            0.99
        }

        velocity = vec3d.multiply(slowdown).subtract(0.0, 0.05000000074505806, 0.0)
        setPosition(x, y, z)
        landed = landed || y < 0
    }

    fun setProperties(
        user: Entity,
        pitch: Float,
        yaw: Float,
        speed: Float
    ) {
        val i = -MathHelper.sin(yaw * 0.017453292f) * MathHelper.cos(pitch * 0.017453292f)
        val j = -MathHelper.sin(pitch * 0.017453292f)
        val k = MathHelper.cos(yaw * 0.017453292f) * MathHelper.cos(pitch * 0.017453292f)

        setVelocity(i.toDouble(), j.toDouble(), k.toDouble(), speed)

        this.setVelocity(i.toDouble(), j.toDouble(), k.toDouble(), speed)
        velocity = velocity.add(user.velocity.x, if (user.onGround) 0.0 else user.velocity.y, user.velocity.z)

        divergence = _divergence * speed
    }

    private fun setPosition(x: Double, y: Double, z: Double) {
        this.x = x
        this.y = y
        this.z = z
        val f: Float = this.dimensions.width / 2.0f
        val g: Float = this.dimensions.height
        boundingBox = Box(x - f.toDouble(), y, z - f.toDouble(), x + f.toDouble(), y + g.toDouble(), z + f.toDouble())
    }

    private fun setVelocity(
        x: Double,
        y: Double,
        z: Double,
        speed: Float
    ) {
        val vec3d = Vec3d(x, y, z).normalize().multiply(speed.toDouble())
        velocity = vec3d
        correctYawPitch(vec3d)
    }

}
