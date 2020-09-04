package me.zeroeightsix.kami.feature.hidden

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.Listenable
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.PacketEvent
import me.zeroeightsix.kami.feature.Feature
import me.zeroeightsix.kami.feature.FindFeature
import me.zeroeightsix.kami.mc
import me.zeroeightsix.kami.mixin.client.IPlayerMoveC2SPacket
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket
import net.minecraft.util.math.Vec3d

typealias YawPitch = Pair<Float, Float>
typealias Position = Vec3d

@FindFeature
object PlayerMovementSpoofer : Feature, Listenable {

    object Priority {
        const val HIGH = 100
        const val SEMIHIGH = 75
        const val NORMAL = 50
        const val LOW = 0
    }

    enum class Mode {
        /**
         * Modify the next movement packet sent. Does not effect packets sent in [INSTANT] mode.
         */
        MODIFY,

        /**
         * Send a packet right now, without cancelling the next packet sent by minecraft.
         */
        INSTANT,

        /**
         * Send a packet right now, and skip the next packet sent by minecraft.
         */
        INSTANT_SKIPNEXT
    }

    override var name: String = "PMQ"
    override var hidden: Boolean = true

    private val onGround: Boolean
        get() = mc.player?.isOnGround ?: true

    // Both rotation and position have a priority: some modules might be considered
    private var rotation: YawPitch? = null
    private var rotationPriority = -1

    private var position: Position? = null
    private var positionPriority = -1

    private var skipNext = false

    @EventHandler
    val packetListener = Listener<PacketEvent.Send>({ event ->
        val packet = event.packet
        if (packet is PlayerMoveC2SPacket) {
            if (skipNext) {
                skipNext = false
                return@Listener
            }

            val ipacket = packet as IPlayerMoveC2SPacket
            rotation?.let {
                ipacket.setYaw(it.first)
                ipacket.setPitch(it.second)
                rotationPriority = -1
            }
            position?.let {
                ipacket.x = it.x
                ipacket.y = it.y
                ipacket.z = it.z
                positionPriority = -1
            }
        }
    })

    fun setRotation(yaw: Float, pitch: Float, priorty: Int = Priority.NORMAL, mode: Mode = Mode.MODIFY) {
        when (mode) {
            Mode.MODIFY -> {
                if (priorty > rotationPriority) {
                    rotation = YawPitch(yaw, pitch)
                    rotationPriority = priorty
                }
            }
            else -> {
                mc.networkHandler?.sendPacket(PlayerMoveC2SPacket.LookOnly(yaw, pitch, onGround))
                if (mode == Mode.INSTANT_SKIPNEXT) {
                    skipNext = true
                }
            }
        }
    }

    fun setPosition(position: Vec3d, priorty: Int = Priority.NORMAL, mode: Mode = Mode.MODIFY) {
        when (mode) {
            Mode.MODIFY -> {
                if (priorty > positionPriority) {
                    this.position = position
                    rotationPriority = priorty
                }
            }
            else -> {
                mc.networkHandler?.sendPacket(
                    PlayerMoveC2SPacket.PositionOnly(
                        position.x,
                        position.y,
                        position.z,
                        onGround
                    )
                )
                if (mode == Mode.INSTANT_SKIPNEXT) {
                    skipNext = true
                }
            }
        }
    }

    fun setPositionRotation(
        position: Vec3d,
        yaw: Float,
        pitch: Float,
        priorty: Int = Priority.NORMAL,
        mode: Mode = Mode.MODIFY
    ) {
        when (mode) {
            Mode.MODIFY -> {
                setRotation(yaw, pitch, priorty, mode)
                setPosition(position, priorty, mode)
            }
            else -> {
                mc.networkHandler?.sendPacket(
                    PlayerMoveC2SPacket.Both(
                        position.x,
                        position.y,
                        position.z,
                        yaw,
                        pitch,
                        onGround
                    )
                )
                if (mode == Mode.INSTANT_SKIPNEXT) {
                    skipNext = true
                }
            }
        }
    }

}
