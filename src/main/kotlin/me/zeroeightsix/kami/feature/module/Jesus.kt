package me.zeroeightsix.kami.feature.module

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.AddCollisionBoxToListEvent
import me.zeroeightsix.kami.event.KamiEvent
import me.zeroeightsix.kami.event.PacketEvent.Send
import me.zeroeightsix.kami.event.TickEvent.Client.InGame
import me.zeroeightsix.kami.mixin.client.IPlayerMoveC2SPacket
import me.zeroeightsix.kami.util.EntityUtil
import me.zeroeightsix.kami.util.Wrapper
import net.minecraft.block.FluidBlock
import net.minecraft.entity.Entity
import net.minecraft.entity.vehicle.BoatEntity
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.MathHelper

@Module.Info(
    name = "Jesus",
    description = "Allows you to walk on water",
    category = Module.Category.MOVEMENT
)
object Jesus : Module() {
    @EventHandler
    private val updateListener = Listener(
        EventHook<InGame> { event: InGame? ->
            if (!Freecam.enabled) {
                if (EntityUtil.isInWater(mc.player) && !mc.player!!.isSneaking) {
                    EntityUtil.updateVelocityY(mc.player, 0.1)
                    if (mc.player!!.vehicle != null && mc.player!!.vehicle !is BoatEntity) {
                        EntityUtil.updateVelocityY(
                            mc.player!!.vehicle,
                            0.3
                        )
                    }
                }
            }
        }
    )

    @EventHandler
    var addCollisionBoxToListEventListener =
        Listener(
            EventHook { event: AddCollisionBoxToListEvent ->
                if (mc.player != null && event.block is FluidBlock
                    && (EntityUtil.isDrivenByPlayer(event.entity) || event.entity === mc.player)
                    && event.entity !is BoatEntity
                    && !mc.player!!.isSneaking
                    && mc.player!!.fallDistance < 3 && !EntityUtil.isInWater(mc.player)
                    && (EntityUtil.isAboveWater(
                        mc.player,
                        false
                    ) || EntityUtil.isAboveWater(
                        mc.player!!.vehicle,
                        false
                    ))
                    && isAboveBlock(
                        mc.player,
                        event.pos
                    )
                ) {
                    val axisalignedbb = WATER_WALK_AA.offset(event.pos)
                    if (event.entityBox.intersects(axisalignedbb)) event.collidingBoxes.add(axisalignedbb)
                    event.cancel()
                }
            }
        )

    @EventHandler
    var packetEventSendListener = Listener(
        EventHook { event: Send ->
            if (event.era === KamiEvent.Era.PRE) {
                if (event.packet is PlayerMoveC2SPacket) {
                    if (EntityUtil.isAboveWater(
                            mc.player,
                            true
                        ) && !EntityUtil.isInWater(mc.player) && !isAboveLand(
                            mc.player
                        )
                    ) {
                        val ticks = mc.player!!.age % 2
                        if (ticks == 0) {
                            val xyz = event.packet as IPlayerMoveC2SPacket
                            xyz.y = xyz.y + 0.02
                        }
                    }
                }
            }
        }
    )

    private val WATER_WALK_AA =
        Box(0.0, 0.0, 0.0, 1.0, 0.99, 1.0)

    private fun isAboveLand(entity: Entity?): Boolean {
        if (entity == null) return false
        val y = entity.y - 0.01
        for (x in MathHelper.floor(entity.x) until MathHelper.ceil(entity.x)) for (z in MathHelper.floor(
            entity.z
        ) until MathHelper.ceil(entity.z)) {
            val pos = BlockPos(x, MathHelper.floor(y), z)

            //if (Wrapper.getWorld().getBlockState(pos).getBlock().isFullOpaque(Wrapper.getWorld().getBlockState(pos), EmptyBlockView.INSTANCE, pos)) return true;
            if (Wrapper.getWorld().getBlockState(pos).isOpaque) return true
        }
        return false
    }

    private fun isAboveBlock(entity: Entity?, pos: BlockPos): Boolean {
        return entity!!.y >= pos.y
    }
}
