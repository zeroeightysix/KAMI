package me.zeroeightsix.kami.feature.module

import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting
import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.events.PacketEvent
import net.minecraft.client.network.packet.*

/**
 * Created by 086 on 4/02/2018.
 * Rewrote and added features by dominikaaaa on 26/07/20
 */
@Module.Info(
    name = "NoRender",
    category = Module.Category.RENDER,
    description = "Prevent rendering of certain things"
)
object NoRender : Module() {
    @Setting
    private var mobs = false

    @Setting
    private var globalEntity = false

    @Setting
    private var objects = false

    @Setting
    private var items = false

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
    private val receiveListener = Listener(EventHook { event: PacketEvent.Receive ->
        val packet = event.packet
        when {
            packet is MobSpawnS2CPacket && mobs -> event.cancel()
            packet is EntitySpawnGlobalS2CPacket && globalEntity -> event.cancel()
            packet is EntitySpawnS2CPacket && objects -> event.cancel()
            /* TODO: can't find item packet */
            packet is ExperienceOrbSpawnS2CPacket && experienceOrbs -> event.cancel()
            packet is ExplosionS2CPacket && explosions -> event.cancel()
            packet is LightUpdateS2CPacket && skyLightUpdates -> event.cancel()
        }
    })
}