package me.zeroeightsix.kami.feature.module

import com.mojang.blaze3d.platform.GlStateManager.*
import com.mojang.blaze3d.systems.RenderSystem
import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting
import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.*
import me.zeroeightsix.kami.event.RenderEvent
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexFormats
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.fluid.FluidState
import net.minecraft.item.*
import net.minecraft.tag.FluidTags
import net.minecraft.util.Hand
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.*
import net.minecraft.world.RayTraceContext
import net.minecraft.world.World
import org.lwjgl.opengl.GL11
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Module.Info(
    name = "Trajectories",
    category = Module.Category.RENDER
)
object Trajectories : Module() {

    @Setting
    private var lineColour = Colour(1f, 1f, 1f, 1f)

    private fun LivingEntity.getHeldItem(): ItemStack? {
        return if (isUsingItem) {
            activeItem
        } else {
            getStackInHand(Hand.MAIN_HAND) ?: getStackInHand(Hand.OFF_HAND)
        }
    }

    /**
     * Modified version of [BowItem.getPullProgress]
     *
     * Takes a float instead of int so tickDelta can be used here for interpolation
     */
    private fun getPullProgress(useTicks: Float): Float {
        var f = useTicks / 20.0f
        f = (f * f + f * 2.0f) / 3.0f
        if (f > 1.0f) {
            f = 1.0f
        }
        return f
    }

    @EventHandler
    var worldListener = Listener(EventHook<RenderEvent.World> {
        val camera = mc.gameRenderer.camera
        val cX = camera.pos.x
        val cY = camera.pos.y
        val cZ = camera.pos.z

        val tessellator = Tessellator.getInstance()
        val buffer = tessellator.buffer
        val matrices = it.matrixStack

        RenderSystem.shadeModel(GL11.GL_SMOOTH)
        RenderSystem.enableAlphaTest()
        RenderSystem.defaultAlphaFunc()
        RenderSystem.disableTexture()
        RenderSystem.enableDepthTest()
        lineWidth(0.5F)

        noBobbingCamera(matrices) {
            mc.world?.entities
                ?.filterIsInstance<LivingEntity>()
                ?.forEach {
                    val stack = it.getHeldItem() ?: return@forEach
                    val mimic = when (stack.item) {
                        is BowItem, is TridentItem -> {
                            if (!it.isUsingItem) return@forEach

                            val isBow = stack.item is BowItem

                            val power = if (isBow) {
                                getPullProgress(stack.maxUseTime - it.itemUseTimeLeft + mc.tickDelta) * 3
                            } else {
                                2.5f + EnchantmentHelper.getRiptide(stack) * 0.5f
                            }

                            val mimic = ProjectileMimic(
                                mc?.world!!,
                                it,
                                if (isBow) {
                                    EntityType.ARROW
                                } else {
                                    EntityType.TRIDENT
                                },
                                if (isBow) {
                                    0.6
                                } else {
                                    0.99
                                },
                                1.0
                            )
                            mimic.setProperties(
                                it,
                                it.pitch,
                                it.yaw,
                                power
                            )

                            mimic
                        }
                        is SnowballItem, is EggItem, is EnderPearlItem, is ExperienceBottleItem, is ThrowablePotionItem -> {
                            val type = when (stack.item) {
                                is SnowballItem -> EntityType.SNOWBALL
                                is EggItem -> EntityType.EGG
                                is EnderPearlItem -> EntityType.ENDER_PEARL
                                is ExperienceBottleItem -> EntityType.EXPERIENCE_BOTTLE
                                is ThrowablePotionItem -> EntityType.POTION
                                else -> unreachable()
                            }

                            val (power, pitchOffset, gravity) = when (stack.item) {
                                is ExperienceBottleItem -> Triple(0.7f, -20f, 0.06)
                                is ThrowablePotionItem -> Triple(0.5f, -20f, 0.06)
                                else -> Triple(1.5f, 0f, 0.03)
                            }

                            val mimic = ThrowableMimic(mc.world!!, it, type, gravity, 1.0)

                            mimic.setProperties(
                                it.pitch,
                                it.yaw,
                                pitchOffset,
                                power
                            )

                            mimic
                        }
                        /*
                        Crossbow issues:
                            The line expects the object to be from the right side of the screen, but the crossbow is in the center of the screen
                            The power values aren't correct. Fireworks travel in a mostly straight line, and arrows go further than the line says
                        */
                        /*is CrossbowItem -> {
                            if (CrossbowItem.isCharged(stack)) {
                                val type = if (stack.item === Items.CROSSBOW && CrossbowItem.hasProjectile(stack, Items.FIREWORK_ROCKET)) EntityType.FIREWORK_ROCKET else EntityType.ARROW
                                val power = if (stack.item === Items.CROSSBOW && CrossbowItem.hasProjectile(stack, Items.FIREWORK_ROCKET)) 1.6f else 3.15f
                                val mimic = ProjectileMimic(mc.world!!, it, type, 0.6, 1.0)

                                mimic.setProperties(
                                    it,
                                    it.pitch,
                                    it.yaw,
                                    power
                                )

                                mimic
                            } else {
                                return@forEach
                            }
                        }*/
                        else -> return@forEach
                    }

                    var offset = if (it == mc.player) {
                        Vec3d(-0.1, 0.075, 0.0)
                            .rotateX((-Math.toRadians(mc.player!!.pitch.toDouble())).toFloat())
                            .rotateY((-Math.toRadians(mc.player!!.yaw.toDouble())).toFloat())
                    } else {
                        Vec3d(0.0, 0.0, 0.0)
                    }

                    buffer.begin(GL11.GL_LINE_STRIP, VertexFormats.POSITION_COLOR)
                    while (!mimic.landed) {
                        buffer.vertex(mimic.x - cX + offset.x, mimic.y - cY + offset.y, mimic.z - cZ + offset.z)
                            .color(lineColour)
                            .next()
                        mimic.tick()
                        offset *= 0.8
                    }
                    tessellator.draw()

                    mimic.hit?.let { hit ->
                        matrices.matrix {
                            translated(hit.x - cX, hit.y - cY, hit.z - cZ)
                            scaled(mimic.diverged, mimic.diverged, mimic.diverged)
                            mimic.face?.let {
                                rotatef(-it.asRotation(), 0.0f, 1.0f, 0.0f)
                                if (it == Direction.DOWN || it == Direction.UP) {
                                    rotatef(90f, 1f, 0f, 0f)
                                }
                            }

                            RenderSystem.disableCull()
                            RenderSystem.disableDepthTest()

                            // I'd love to use a VBO for this, but I'm not entirely sure how to use minecraft's `VertexBuffer`.
                            with(buffer) {
                                begin(GL11.GL_TRIANGLE_FAN, VertexFormats.POSITION_COLOR)

                                vertex(0.0, 0.0, 0.0).color(1f, 1f, 1f, 0.4f).next()
                                for (angle in 0..24) {
                                    val angle = (angle.toDouble() / 24.0) * 2 * PI
                                    vertex(sin(angle), cos(angle), 0.0).color(1f, 1f, 1f, 0.4f).next()
                                }

                                tessellator.draw()
                            }

                            RenderSystem.enableDepthTest()
                            RenderSystem.enableCull()
                        }
                    }
                }
        }

        RenderSystem.lineWidth(1.0f)
        RenderSystem.enableBlend()
        RenderSystem.enableTexture()
        RenderSystem.shadeModel(GL11.GL_FLAT)
    })
}

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
        val pos = shooter.getInterpolatedPos()
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
        velocity = velocity.add(user.velocity.x, if (user.isOnGround) 0.0 else user.velocity.y, user.velocity.z)

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
        val pos = shooter.getInterpolatedPos()
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
        velocity = velocity.add(0.0, if (shooter.isOnGround) 0.0 else shooter.velocity.y, 0.0)
    }

    override fun tick() {
        val box: Box = boundingBox.stretch(velocity).expand(1.0)
        val collisions =
            world.getOtherEntities(null, box) { entityx: Entity -> !entityx.isSpectator && entityx.collides() }
                .filterNot { it.isSpectator || !it.collides() || it == shooter }

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
        val pooledMutable = BlockPos.Mutable()
        try {
            for (p in i until j) {
                for (q in k until l) {
                    for (r in m until n) {
                        pooledMutable.set(p, q, r)
                        val fluidState: FluidState? = mc.world?.getFluidState(pooledMutable)
                        if (fluidState != null) {
                            if (fluidState.isIn(FluidTags.WATER)) {
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
            }
        } catch (e: Throwable) {
        } finally {
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
