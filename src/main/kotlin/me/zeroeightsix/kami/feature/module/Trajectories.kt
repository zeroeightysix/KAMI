package me.zeroeightsix.kami.feature.module

import com.mojang.blaze3d.platform.GlStateManager
import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.ProjectileMimic
import me.zeroeightsix.kami.event.events.RenderEvent
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexFormats
import net.minecraft.item.BowItem
import net.minecraft.util.math.Vec3d
import org.lwjgl.opengl.GL11

/**
 * Created by 086 on 28/12/2017.
 */
@Module.Info(
    name = "Trajectories",
    category = Module.Category.RENDER
)
object Trajectories : Module() {
    private var positions = mutableListOf<Vec3d>()

    fun getPullProgress(useTicks: Float): Float {
        var f = useTicks / 20.0f
        f = (f * f + f * 2.0f) / 3.0f
        if (f > 1.0f) {
            f = 1.0f
        }
        return f
    }

    @EventHandler
    var worldListener =
        Listener(
            EventHook<RenderEvent.World> {
                if ((mc.player.activeItem ?: return@EventHook).item is BowItem && mc.player.isUsingItem) {
                    val pullProgress =
                        getPullProgress(mc.player.activeItem.maxUseTime - mc.player.itemUseTimeLeft + mc.tickDelta)
                    val projectileEntity = ProjectileMimic(mc.world, mc.player)
                    projectileEntity.setProperties(
                        mc.player,
                        mc.player.pitch,
                        mc.player.yaw,
                        pullProgress * 3.0f
                    )

                    val camera = mc.gameRenderer.camera
                    val cX = camera.pos.x
                    val cY = camera.pos.y
                    val cZ = camera.pos.z

                    val tesselator = Tessellator.getInstance()
                    val buffer = tesselator.bufferBuilder

                    var eyes: Vec3d = Vec3d(-0.1, 0.075, 0.0)
                        .rotateX((-Math.toRadians(MinecraftClient.getInstance().player.pitch.toDouble())).toFloat())
                        .rotateY((-Math.toRadians(MinecraftClient.getInstance().player.yaw.toDouble())).toFloat())

                    GlStateManager.enableDepthTest()
                    GlStateManager.lineWidth(1.5f)
                    buffer.begin(GL11.GL_LINE_STRIP, VertexFormats.POSITION_COLOR)
                    while (!projectileEntity.landed) {
                        buffer.vertex(
                            projectileEntity.x - cX + eyes.x,
                            projectileEntity.y - cY + eyes.y,
                            projectileEntity.z - cZ + eyes.z
                        )
                            .color(1f, 1f, 1f, 1f - eyes.lengthSquared().toFloat())
                            .next()
                        projectileEntity.tick()
                        eyes = eyes.multiply(0.8)
                    }
                    tesselator.draw()
                }
            }
        )
}
