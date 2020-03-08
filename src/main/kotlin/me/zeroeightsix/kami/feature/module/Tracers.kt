package me.zeroeightsix.kami.feature.module

import com.mojang.blaze3d.platform.GlStateManager
import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.events.RenderEvent
import me.zeroeightsix.kami.event.events.TickEvent
import me.zeroeightsix.kami.mixin.extend.applyCameraTransformations
import me.zeroeightsix.kami.mixin.extend.getRenderPosX
import me.zeroeightsix.kami.mixin.extend.getRenderPosY
import me.zeroeightsix.kami.mixin.extend.getRenderPosZ
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.ColourUtils
import me.zeroeightsix.kami.util.EntityUtil
import me.zeroeightsix.kami.util.Friends
import me.zeroeightsix.kami.util.HueCycler
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.Vec3d
import org.lwjgl.opengl.GL11

/**
 * Created by 086 on 11/12/2017.
 */
@Module.Info(
    name = "Tracers",
    description = "Draws lines to other living entities",
    category = Module.Category.RENDER
)
object Tracers : Module() {
    private val players = register(Settings.b("Players", true))
    private val friends = register(Settings.b("Friends", true))
    private val animals = register(Settings.b("Animals", false))
    private val mobs = register(Settings.b("Mobs", false))
    private val range = register(Settings.d("Range", 200.0))
    private val opacity: Setting<Float> = register(Settings.floatBuilder("Opacity").withRange(0f, 1f).withValue(1f).build() as Setting<Float>)
    var cycler = HueCycler(3600)

    @EventHandler
    val worldListener = Listener(
        EventHook<RenderEvent> {
            GlStateManager.pushMatrix()
            MinecraftClient.getInstance().world.entities
                .filter { EntityUtil.isLiving(it) && !EntityUtil.isFakeLocalPlayer(it) }
                .filter {
                    when {
                        it is PlayerEntity -> players.value && mc.player !== it
                        EntityUtil.isPassive(
                            it
                        ) -> animals.value
                        else -> mobs.value
                    }
                }
                .filter { mc.player.distanceTo(it) < range.value }
                .forEach {
                    var colour = getColour(it)
                    if (colour == ColourUtils.Colors.RAINBOW) {
                        if (!friends.value) return@forEach
                        colour = cycler.current()
                    }
                    val r = (colour ushr 16 and 0xFF) / 255f
                    val g = (colour ushr 8 and 0xFF) / 255f
                    val b = (colour and 0xFF) / 255f
                    drawLineToEntity(it, r, g, b, opacity.value)
                }
            GlStateManager.popMatrix()
        }
    )

    @EventHandler
    val updateListener = Listener(
        EventHook<TickEvent.Client.InGame> {
            cycler.next()
        }
    )

    private fun drawRainbowToEntity(entity: Entity, opacity: Float) {
        val eyes: Vec3d = Vec3d(0.0, 0.0, 1.0)
            .rotateY(
                (-Math
                    .toRadians(MinecraftClient.getInstance().player.pitch.toDouble())).toFloat()
            )
            .rotateX(
                (-Math
                    .toRadians(MinecraftClient.getInstance().player.yaw.toDouble())).toFloat()
            )
        val xyz = interpolate(entity)
        val posx = xyz[0]
        val posy = xyz[1]
        val posz = xyz[2]
        val posx2 = eyes.x
        val posy2: Double = eyes.y + mc.player.getEyeHeight(mc.player.pose)
        val posz2 = eyes.z
        GL11.glBlendFunc(770, 771)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glLineWidth(1.5f)
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glDisable(GL11.GL_DEPTH_TEST)
        GL11.glDepthMask(false)
        cycler.reset()
        cycler.setNext(opacity)
        GlStateManager.disableLighting()
        GL11.glLoadIdentity()
        mc.gameRenderer.applyCameraTransformations(mc.tickDelta)
        GL11.glBegin(GL11.GL_LINES)
        run {
            GL11.glVertex3d(posx, posy, posz)
            GL11.glVertex3d(posx2, posy2, posz2)
            cycler.setNext(opacity)
            GL11.glVertex3d(posx2, posy2, posz2)
            GL11.glVertex3d(posx2, posy2, posz2)
        }
        GL11.glEnd()
        GL11.glEnable(GL11.GL_TEXTURE_2D)
        GL11.glEnable(GL11.GL_DEPTH_TEST)
        GL11.glDepthMask(true)
        GL11.glDisable(GL11.GL_BLEND)
        GL11.glColor3d(1.0, 1.0, 1.0)
        GlStateManager.enableLighting()
    }

    private fun getColour(entity: Entity): Int {
        return if (entity is PlayerEntity) {
            if (Friends.isFriend(entity.gameProfile.name)) ColourUtils.Colors.RAINBOW else ColourUtils.Colors.WHITE
        } else {
            if (EntityUtil.isPassive(entity)) ColourUtils.Colors.GREEN else ColourUtils.Colors.RED
        }
    }

    fun interpolate(now: Double, then: Double): Double {
        return then + (now - then) * mc.tickDelta
    }

    private fun interpolate(entity: Entity): DoubleArray {
        val x = interpolate(
            entity.x,
            entity.prevRenderX
        ) - mc.entityRenderManager.getRenderPosX()
        val y = interpolate(
            entity.y,
            entity.prevRenderY
        ) - mc.entityRenderManager.getRenderPosY()
        val z = interpolate(
            entity.z,
            entity.prevRenderZ
        ) - mc.entityRenderManager.getRenderPosZ()
        return doubleArrayOf(x, y, z)
    }

    fun drawLineToEntity(
        e: Entity,
        red: Float,
        green: Float,
        blue: Float,
        opacity: Float
    ) {
        val xyz = interpolate(e)
        drawLine(xyz[0], xyz[1], xyz[2], e.height.toDouble(), red, green, blue, opacity)
    }

    fun drawLine(
        posx: Double,
        posy: Double,
        posz: Double,
        up: Double,
        red: Float,
        green: Float,
        blue: Float,
        opacity: Float
    ) {
        val eyes: Vec3d = Vec3d(0.0, 0.0, 1.0)
            .rotateY(
                (-Math
                    .toRadians(MinecraftClient.getInstance().player.pitch.toDouble())).toFloat()
            )
            .rotateX(
                (-Math
                    .toRadians(MinecraftClient.getInstance().player.yaw.toDouble())).toFloat()
            )
        drawLineFromPosToPos(
            eyes.x,
            eyes.y + mc.player.getEyeHeight(mc.player.pose),
            eyes.z,
            posx,
            posy,
            posz,
            up,
            red,
            green,
            blue,
            opacity
        )
    }

    fun drawLineFromPosToPos(
        posx: Double,
        posy: Double,
        posz: Double,
        posx2: Double,
        posy2: Double,
        posz2: Double,
        up: Double,
        red: Float,
        green: Float,
        blue: Float,
        opacity: Float
    ) {
        GL11.glBlendFunc(770, 771)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glLineWidth(1.5f)
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glDisable(GL11.GL_DEPTH_TEST)
        GL11.glDepthMask(false)
        GL11.glColor4f(red, green, blue, opacity)
        GlStateManager.disableLighting()
        GL11.glLoadIdentity()
        mc.gameRenderer.applyCameraTransformations(mc.tickDelta)
        GL11.glBegin(GL11.GL_LINES)
        run {
            GL11.glVertex3d(posx, posy, posz)
            GL11.glVertex3d(posx2, posy2, posz2)
            GL11.glVertex3d(posx2, posy2, posz2)
            GL11.glVertex3d(posx2, posy2 + up, posz2)
        }
        GL11.glEnd()
        GL11.glEnable(GL11.GL_TEXTURE_2D)
        GL11.glEnable(GL11.GL_DEPTH_TEST)
        GL11.glDepthMask(true)
        GL11.glDisable(GL11.GL_BLEND)
        GL11.glColor3d(1.0, 1.0, 1.0)
        GlStateManager.enableLighting()
    }
}