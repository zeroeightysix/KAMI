package me.zeroeightsix.kami.mimic

import me.zeroeightsix.kami.util.EntityUtil
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.util.math.Box
import net.minecraft.util.math.Direction
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World

class ThrowableMimic(
    val world: World,
    private val shooter: LivingEntity,
    val type: EntityType<*>,
    private val gravity: Double,
    private val _divergence: Double
) : TrajectoryMimic {

    override var x = 0.0
    override var y = 0.0
    override var z = 0.0
    override var yaw = 0f
    override var pitch = 0f
    override var prevYaw = 0f
    override var prevPitch = 0f
    override var landed = false
    override var entity: Entity? = null
    override var diverged = 0.0
    override var face: Direction? = null
    override var hit: Vec3d? = null

    private lateinit var velocity: Vec3d
    private lateinit var boundingBox: Box
    private val dimensions = type.dimensions
    private var divergence: Double = 0.0

    init {
        val pos = EntityUtil.getInterpolatedPos(shooter, MinecraftClient.getInstance().tickDelta)
        setPosition(pos.x, pos.y + shooter.standingEyeHeight.toDouble() - 0.10000000149011612, pos.z)
    }

    private fun setPosition(x: Double, y: Double, z: Double) {
        this.x = x
        this.y = y
        this.z = z
        val f = dimensions.width / 2.0f
        val g = dimensions.height
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

    fun setProperties(
        pitch: Float,
        yaw: Float,
        pitchOffset: Float,
        power: Float
    ) {
        val i = -MathHelper.sin(yaw * 0.017453292f) * MathHelper.cos(pitch * 0.017453292f)
        val j = -MathHelper.sin((pitch + pitchOffset) * 0.017453292f)
        val k = MathHelper.cos(yaw * 0.017453292f) * MathHelper.cos(pitch * 0.017453292f)
        this.setVelocity(i.toDouble(), j.toDouble(), k.toDouble(), power)
        divergence = power * _divergence
//        velocity = velocity.add(shooter.velocity.x, if (shooter.onGround) 0.0 else shooter.velocity.y, shooter.velocity.z)
        velocity = velocity.add(0.0, if (shooter.onGround) 0.0 else shooter.velocity.y, 0.0)
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

        if (checkCollision(velocity, shooter, world)) return

        val vec3d: Vec3d = velocity

        diverged += vec3d.length() * divergence * 0.007499999832361937

        x += vec3d.x
        y += vec3d.y
        z += vec3d.z
        val f = MathHelper.sqrt(Entity.squaredHorizontalLength(vec3d))
        yaw = (MathHelper.atan2(vec3d.x, vec3d.z) * 57.2957763671875).toFloat()
        pitch = (MathHelper.atan2(vec3d.y, f.toDouble()) * 57.2957763671875).toFloat()

        dropPitchAndYaw()

        val slowdown = if (isInWater(boundingBox)) {
            0.8
        } else {
            0.99
        }

        velocity = vec3d.multiply(slowdown).subtract(0.0, gravity, 0.0)
        setPosition(x, y, z)
        landed = landed || y < 0
    }

}
