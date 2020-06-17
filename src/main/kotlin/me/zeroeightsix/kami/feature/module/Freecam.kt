package me.zeroeightsix.kami.feature.module

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.fiber.api.annotation.Setting
import me.zeroeightsix.fiber.api.annotation.Settings
import me.zeroeightsix.kami.event.events.PacketEvent.Send
import me.zeroeightsix.kami.event.events.PlayerMoveEvent
import me.zeroeightsix.kami.event.events.TickEvent
import net.minecraft.client.network.OtherClientPlayerEntity
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.network.packet.PlayerInputC2SPacket
import net.minecraft.server.network.packet.PlayerMoveC2SPacket
import net.minecraft.util.math.Vec3d

/**
 * Created by 086 on 22/12/2017.
 */
@Module.Info(
    name = "Freecam",
    category = Module.Category.PLAYER,
    description = "Leave your body and trascend into the realm of the gods"
)
@Settings(onlyAnnotated = true)
object Freecam : Module() {
    @Setting
    private var speed = 5
    private var x = 0.0
    private var y = 0.0
    private var z = 0.0
    private var pitch = 0f
    private var yaw = 0f
    private var clonedPlayer: OtherClientPlayerEntity? = null
    private var isRidingEntity = false
    private var ridingEntity: Entity? = null

    override fun onEnable() {
        if (mc.player != null) {
            isRidingEntity = mc.player.vehicle != null
            if (mc.player.vehicle == null) {
                x = mc.player.x
                y = mc.player.y
                z = mc.player.z
            } else {
                ridingEntity = mc.player.vehicle
                mc.player.stopRiding()
            }
            pitch = mc.player.pitch
            yaw = mc.player.yaw
            clonedPlayer = OtherClientPlayerEntity(
                mc.world,
                mc.session.profile
            )
            clonedPlayer!!.copyFrom(mc.player)
            clonedPlayer!!.headYaw = mc.player.headYaw
            mc.world.addEntity(-100, clonedPlayer)
            mc.player.abilities.flying = true
            mc.player.abilities.flySpeed = speed / 100f
            mc.player.noClip = true
        }
    }

    override fun onDisable() {
        val localPlayer: PlayerEntity? = mc.player
        if (localPlayer != null) {
            mc.player.setPositionAndAngles(
                x,
                y,
                z,
                yaw,
                pitch
            )
            mc.world.removeEntity(-100)
            clonedPlayer = null
            z = 0.0
            y =
                z
            x =
                y
            yaw = 0f
            pitch =
                yaw
            mc.player.abilities.flying =
                false //getModManager().getMod("ElytraFlight").isEnabled();
            mc.player.abilities.flySpeed = 0.05f
            mc.player.noClip = false
            mc.player.velocity = Vec3d.ZERO
            if (isRidingEntity) {
                mc.player.startRiding(ridingEntity, true)
            }
        }
    }

    @EventHandler
    private val updateListener =
        Listener(
            EventHook<TickEvent.Client.InGame> {
                mc.player.abilities.flying = true
                mc.player.abilities.flySpeed = speed / 100f
                mc.player.noClip = true
                mc.player.onGround = false
                mc.player.fallDistance = 0f
            }
        )
    @EventHandler
    private val moveListener =
        Listener(
            EventHook<PlayerMoveEvent> { event: PlayerMoveEvent? ->
                mc.player.noClip = true
            }
        )
    @EventHandler
    private val sendListener = Listener(
        EventHook { event: Send ->
            if (event.packet is PlayerMoveC2SPacket || event.packet is PlayerInputC2SPacket) {
                event.cancel()
            }
        }
    )
}