package me.zeroeightsix.kami.feature.module

import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting
import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.PacketEvent
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket
import net.minecraft.network.packet.s2c.play.ExperienceOrbSpawnS2CPacket
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket
import net.minecraft.network.packet.s2c.play.LightUpdateS2CPacket
import net.minecraft.network.packet.s2c.play.MobSpawnS2CPacket

@Module.Info(
    name = "NoRender",
    category = Module.Category.RENDER,
    description = "Prevent rendering of certain things"
)
object NoRender : Module() {
    @Setting
    private var mobs = false

    @Setting
    private var objects = false

    @Setting
    private var experienceOrbs = true

    @Setting
    var fire = true

    @Setting
    private var explosions = true

    @Setting
    var beaconBeams = false

    @Setting
    private var skyLightUpdates = true

    @EventHandler
    private val receiveListener = Listener({ event: PacketEvent.Receive ->
        val packet = event.packet
        when {
            packet is MobSpawnS2CPacket && mobs -> event.cancel()
            packet is EntitySpawnS2CPacket && objects -> event.cancel()
            packet is ExperienceOrbSpawnS2CPacket && experienceOrbs -> event.cancel()
            packet is ExplosionS2CPacket && explosions -> event.cancel()
            packet is LightUpdateS2CPacket && skyLightUpdates -> event.cancel()
        }
    })
}
