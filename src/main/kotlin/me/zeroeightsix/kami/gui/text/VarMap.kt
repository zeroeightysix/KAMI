package me.zeroeightsix.kami.gui.text

import imgui.ImGui
import me.zeroeightsix.kami.BaritoneIntegration
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.gui.widgets.modulesVariable
import me.zeroeightsix.kami.mc
import me.zeroeightsix.kami.mixin.client.IMinecraftClient
import me.zeroeightsix.kami.put
import me.zeroeightsix.kami.replaceAll
import me.zeroeightsix.kami.feature.hidden.TickSpeedMeter
import me.zeroeightsix.kami.util.modified
import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier
import net.minecraft.util.math.Direction
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import net.minecraft.util.registry.Registry
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.math.floor

/**
 * The global 'variable map', used in various widgets.
 *
 * The varmap contains variable producers and not single instances of variables. Thus, it is okay to mutate variables gotten from the varmap.
 */
object VarMap {

    private val ampmFormat = SimpleDateFormat("hh:mm:ss aa")
    private val militaryFormat = SimpleDateFormat("HH:mm:ss")

    private infix fun String.const(strProvider: () -> String) =
        this to { CompiledText.ConstantVariable(this, string = strProvider()) }

    private infix fun String.numeric(valProvider: () -> Double) =
        this to { CompiledText.NumericalVariable(this, valProvider, 0) }

    private infix fun String.string(strProvider: () -> String) =
        this to { CompiledText.StringVariable(this, provider = strProvider) }

    private fun toMb(bytes: Long) = (bytes / 1e+6)
    private fun scaleDimensional(value: Double) = value * (
        mc.world?.let {
            if (it.registryKey.value.path == "overworld")
                1.0 / 8.0
            else
                it.dimension.coordinateScale
        } ?: 1.0
        )

    // KAMI will almost always be used on vanilla, so we strip the minecraft namespace from identifiers.
    // If it's not in the minecraft namespace, we return the default toString of the identifier.
    private fun Identifier.stripMinecraftNamespace(): String {
        if (this.namespace == "minecraft") return this.path
        return toString()
    }

    private val ItemStack.filterAir: ItemStack?
        get() = if (isEmpty) null else this

    // Speed in meter per second.
    private val speedMps: Double
        get() = (mc.player?.vehicle?.velocity ?: mc.player?.velocity ?: Vec3d.ZERO).modified(y = 0.0)
            .length() * 20.0 / 0.546 // *20 because 20 ticks in 1 sec, and velocity is tick-based. I don't know where the 0.546 error factor comes from. I got it by comparing what I got without it with the values from the wiki, and they match up nicely.

    internal val inner: MutableMap<String, () -> CompiledText.Variable> = mutableMapOf(
        "none" const { "No variable selected " },
        "x" numeric { mc.player?.pos?.x ?: 0.0 },
        "y" numeric { mc.player?.pos?.y ?: 0.0 },
        "z" numeric { mc.player?.pos?.z ?: 0.0 },
        "dimensional_x" numeric { scaleDimensional(mc.player?.pos?.x ?: 0.0) },
        "dimensional_z" numeric { scaleDimensional(mc.player?.pos?.z ?: 0.0) },
        "yaw" numeric { MathHelper.wrapDegrees(mc.player?.yaw?.toDouble() ?: 0.0) },
        "pitch" numeric { mc.player?.pitch?.toDouble() ?: 0.0 },
        "tps" numeric { TickSpeedMeter.tickRate.toDouble() },
        "fps" numeric { IMinecraftClient.getCurrentFps().toDouble() },
        // Minecraft's fps is 'frames in last second', while imgui calculates an average over the last 120 frames.
        // This varies more, but it provides a new value every frame, thus perfect for graphs.
        "fps_fast" numeric { ImGui.io.framerate.toDouble() },
        "ping" numeric {
            mc.player?.let {
                mc.networkHandler?.getPlayerListEntry(it.uuid)?.latency?.toDouble()
            } ?: -1.0
        },
        "server_brand" string { mc.player?.serverBrand.toString() },
        "server_name" string { mc.currentServerEntry?.name ?: "Singleplayer" },
        "server_ip" string { mc.currentServerEntry?.address ?: "Offline" },
        "server_version" string { mc.currentServerEntry?.version?.string ?: mc.gameVersion ?: "" },
        "username" const { mc.session.username },
        "version" const { KamiMod.MODVER },
        "client" const { KamiMod.MODNAME },
        "kanji" const { KamiMod.KAMI_KANJI },
        "modules" to { modulesVariable },
        "speed_kmh" numeric { speedMps * 3.6 },
        "speed_mps" numeric { speedMps },
        // what's wrong with americans
        "speed_mph" numeric { speedMps * 2.23694 },
        // maybe i should implement a 'calculation' part that runs an equation on some user-defined variables.. jk.. unless? :flushed:
        "memory_used_mb" numeric { toMb(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) },
        "memory_free_mb" numeric { toMb(Runtime.getRuntime().freeMemory()) },
        "memory_total_mb" numeric { toMb(Runtime.getRuntime().totalMemory()) },
        "memory_percentage" numeric {
            100.0 - ((Runtime.getRuntime().freeMemory() * 1.0 / Runtime.getRuntime().totalMemory())) * 100.0
        },
        "facing_cardinal" string { mc.player?.horizontalFacing?.toString() ?: "" },
        "facing_axis" string {
            when (mc.player?.horizontalFacing) {
                Direction.NORTH -> "-Z"
                Direction.SOUTH -> "+Z"
                Direction.WEST -> "-X"
                Direction.EAST -> "+X"
                else -> ""
            }
        },
        "durability" numeric {
            (
                (
                    mc.player?.activeItem?.filterAir ?: mc.player?.itemsHand?.mapNotNull { it.filterAir }
                        ?.firstOrNull()
                    )?.let { it.maxDamage - it.damage }
                    ?: 0
                ).toDouble()
        },
        "health" numeric { (mc.player?.health ?: 0f).toDouble() },
        "biome" string {
            mc.world?.registryManager?.get(Registry.BIOME_KEY)?.getId(mc.world?.getBiome(mc.player?.blockPos))
                ?.stripMinecraftNamespace()?.replace("_", " ")
                ?: "unknown biome"
        },
        "day" numeric {
            (mc.world?.timeOfDay ?: 0) / 24000.0
        },
        "dimension" string {
            mc.world?.registryKey?.value?.stripMinecraftNamespace() ?: "unknown dimension"
        },
        "packets_inbound" numeric {
            (mc.networkHandler?.connection?.averagePacketsReceived ?: 0f).toDouble()
        },
        "packets_outbound" numeric {
            (mc.networkHandler?.connection?.averagePacketsSent ?: 0f).toDouble()
        },
        "gametime" string {
            val time = ((mc.world?.timeOfDay ?: 0) % 24000.0) / 20.0
            val minute = floor(time / 60.0).toInt().toString().padStart(2, '0')
            val second = (time % 60).toInt().toString().padStart(2, '0')
            "$minute:$second"
        },
        "gametime_ticks" numeric { mc.world?.timeOfDay?.toDouble() ?: 0.0 },
        "gametime_day_ticks" numeric { (mc.world?.timeOfDay?.toDouble() ?: 0.0) % 24000.0 },
        "realtime_ampm" string { ampmFormat.format(Date()) },
        "realtime_24h" string { militaryFormat.format(Date()) }
    ).apply {
        BaritoneIntegration {
            put("baritone" string { BaritoneIntegration.recentControlProcess?.displayName() ?: "None" })
            put("baritone_formatted" string {
                BaritoneIntegration.recentControlProcess?.displayName()
                    // remove brackets
                    ?.replaceAll("[]{}".asIterable(), " ")
                    // add space after comma
                    ?.replace(",", ", ")
                    // remove duplicate spaces
                    ?.replace(Regex("\\s+"), " ")
                    // remove spaces before commas
                    ?.replace(" ,", ",") ?: "None"
            })
        }
    }

    operator fun get(variable: String) = inner[variable]
}
