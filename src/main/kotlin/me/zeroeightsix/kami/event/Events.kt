package me.zeroeightsix.kami.event

import io.github.fablabsmc.fablabs.api.fiber.v1.tree.ConfigTree
import me.zeroeightsix.kami.mc
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.client.gui.hud.ClientBossBar
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.input.Input
import net.minecraft.client.network.AbstractClientPlayerEntity
import net.minecraft.client.render.Camera
import net.minecraft.client.render.LightmapTextureManager
import net.minecraft.client.util.Window
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.MovementType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.network.Packet
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket
import net.minecraft.text.Text
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.util.math.*
import net.minecraft.world.BlockView
import net.minecraft.world.World
import net.minecraft.world.chunk.Chunk
import java.util.function.Predicate

class AddCollisionBoxToListEvent(
    val block: Block,
    val state: BlockState,
    val world: World,
    val pos: BlockPos,
    val entityBox: Box,
    val collidingBoxes: MutableList<Box>,
    val entity: Entity,
    val isBool: Boolean
) : KamiEvent()

class ApplyFogEvent : KamiEvent()

class BindEvent(val key: Int, val scancode: Int, i: Int) : KamiEvent() {
    val pressed = i != 0
    val ingame = mc.currentScreen == null
}

class CameraHurtEvent(val tickDelta: Float) : KamiEvent()

class CanBeControlledEvent(val entity: Entity, var canBeSteered: Boolean?) : KamiEvent()

open class ChunkEvent private constructor(val chunk: Chunk?) : KamiEvent() {
    class Load(chunk: Chunk?, val packet: ChunkDataS2CPacket) : ChunkEvent(chunk)
    class Unload(chunk: Chunk?) : ChunkEvent(chunk)
}

class ClipAtLedgeEvent(val player: PlayerEntity, var clip: Boolean? = null) : KamiEvent()

class CloseScreenInPortalEvent(val screen: Screen?) : KamiEvent()

class DisplaySizeChangedEvent

open class EntityEvent(val entity: Entity) : KamiEvent() {

    class EntityCollision(
        entity: Entity,
        var x: Double,
        var y: Double,
        var z: Double
    ) : EntityEvent(entity)

    class EntityDamage(entity: Entity, var damage: Int) : EntityEvent(entity)

}

class EntityJoinWorldEvent(val id: Int, val entity: Entity) : KamiEvent()

class EntityVelocityMultiplierEvent(val entity: Entity?, var multiplier: Float) : KamiEvent()

class InputUpdateEvent(
    val previousState: Input,
    var newState: Input
) : KamiEvent()

class LivingEntityTickEvent(private val entity: LivingEntity) : KamiEvent() {
    fun getEntity(): Entity {
        return entity
    }
}

class MoveEntityFluidEvent(val entity: Entity, var movement: Vec3d) : KamiEvent()

open class PacketEvent(val packet: Packet<*>) : KamiEvent() {

    class Receive(packet: Packet<*>) : PacketEvent(packet)
    class Send(packet: Packet<*>) : PacketEvent(packet)

}

class PlayerAttackBlockEvent(val position: BlockPos, val facing: Direction)

class PlayerAttackEntityEvent(val entity: Entity) : KamiEvent()

class PlayerMoveEvent(val type: MovementType, val vec: Vec3d) : KamiEvent()

open class RenderBossBarEvent : KamiEvent() {
    class GetIterator(var iterator: Iterator<ClientBossBar>) : RenderBossBarEvent()
    class GetText(val bossBar: ClientBossBar, var text: Text) : RenderBossBarEvent()
    class Spacing(var spacing: Int) : RenderBossBarEvent()
}

open class RenderEvent private constructor(private val stage: Stage) : KamiEvent() {
    enum class Stage {
        WORLD, SCREEN
    }

    class Screen : RenderEvent(Stage.SCREEN)
    class World(
        val tickDelta: Float,
        val matrixStack: MatrixStack,
        val projection: Matrix4f
    ) :
        RenderEvent(Stage.WORLD) {

        init {
            era = Era.POST
        }
    }
}

class RenderGuiEvent(
    val window: Window,
    val matrixStack: MatrixStack
) : KamiEvent()

class RenderPlayerNametagEvent(val entity: AbstractClientPlayerEntity) : KamiEvent()

class RenderWeatherEvent(
    val manager: LightmapTextureManager,
    val f: Float,
    val d: Double,
    val e: Double,
    val g: Double
) : KamiEvent()

open class ScreenEvent(var screen: Screen?) : KamiEvent() {

    class Displayed(screen: Screen?) : ScreenEvent(screen)
    class Closed(screen: Screen?) : ScreenEvent(screen)

}

class TargetEntityEvent(
    val entity: Entity,
    val vec3d: Vec3d,
    val vec3d2: Vec3d,
    val box: Box,
    val predicate: Predicate<Entity>,
    val d: Double,
    var trace: EntityHitResult?
) : KamiEvent()

open class TickEvent private constructor(private val stage: Stage) : KamiEvent() {
    enum class Stage {
        CLIENT
    }

    open class Client : TickEvent(Stage.CLIENT) {
        /**
         * This exists because many listeners for TickEvents will perform player null checks.
         * This event is ensured to only fire when the player and world is not null.
         */
        class InGame : Client()

        /**
         * @see InGame
         */
        class OutOfGame : Client()
    }

}

class CameraUpdateEvent(
    val camera: Camera,
    val area: BlockView?,
    val focusedEntity: Entity?,
    val thirdPerson: Boolean,
    val inverseView: Boolean,
    val tickDelta: Float
) : KamiEvent()

class UpdateLookEvent(
    val deltaX: Double,
    val deltaY: Double
) : KamiEvent()

class ConfigSaveEvent(config: ConfigTree?) : KamiEvent()
