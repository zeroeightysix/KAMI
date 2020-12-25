package me.zeroeightsix.kami.feature.module

import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting
import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.TickEvent
import me.zeroeightsix.kami.mc
import me.zeroeightsix.kami.mixin.client.IMinecraftClient
import me.zeroeightsix.kami.util.InstrumentMap
import me.zeroeightsix.kami.util.MidiParser
import me.zeroeightsix.kami.util.Note
import me.zeroeightsix.kami.util.Viewblock.getIrreplaceableNeighbour
import me.zeroeightsix.kami.util.Wrapper
import me.zeroeightsix.kami.util.plus
import me.zeroeightsix.kami.util.text
import net.minecraft.block.Blocks
import net.minecraft.block.NoteBlock.INSTRUMENT
import net.minecraft.block.NoteBlock.NOTE
import net.minecraft.block.enums.Instrument
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.client.world.ClientWorld
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket
import net.minecraft.util.Hand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import java.io.File
import java.util.ArrayList
import java.util.TreeMap

@Module.Info(
    name = "Notebot",
    category = Module.Category.MISC,
    description = "Plays music with Noteblocks; put songs as .mid files in .mincraft/songs"
)
object Notebot : Module() {
    @Setting
    var mode = NotebotMode.DISCOVER

    @Setting(comment = "Name of MIDI file in the songs folder of your .minecraft directory")
    var songName = "tetris_theme.mid"

    @Setting
    var hatAlwaysAsFSharp = true

    @Setting(name = "Channel 0")
    var ChannelZero = Instrument.HAT

    @Setting(name = "Channel 1")
    var ChannelOne = Instrument.HAT

    @Setting(name = "Channel 2")
    var ChannelTwo = Instrument.HAT

    @Setting(name = "Channel 3")
    var ChannelThree = Instrument.HAT

    @Setting(name = "Channel 4")
    var ChannelFour = Instrument.HAT

    private var channelsArray = arrayOf(ChannelZero, ChannelOne, ChannelTwo, ChannelThree, ChannelFour)

    private var lastNote: Long = 0L
    private var elapsed: Long = 0L

    private var noteSequence: TreeMap<Long, ArrayList<Note>> = TreeMap()
    var instrumentMap = InstrumentMap()

    enum class NotebotMode {
        DISCOVER,
        LOAD,
        PLAY
    }

    @EventHandler
    val listener = Listener<TickEvent.InGame>({
        val player = it.player
        val world = it.world

        when (mode) {
            NotebotMode.DISCOVER -> {
                for (x in -4..4) for (y in -4..4) for (z in -4..4) {
                    val pos = player.pos?.plus(Vec3d(x.toDouble(), y.toDouble(), z.toDouble())) ?: break
                    if (world.getBlockState(BlockPos(pos))?.block == Blocks.NOTE_BLOCK) {
                        val noteBlock = world.getBlockState(BlockPos(pos))
                        snackbarMessage(
                            player, "" + noteBlock.get(INSTRUMENT) + "[" + noteBlock.get(NOTE) + "]"
                        )
                        map.add(noteBlock.get(INSTRUMENT), noteBlock.get(NOTE), BlockPos(pos))
                    }
                }
            }
            NotebotMode.LOAD -> {
                noteSequence = MidiParser.parse("songs" + File.separator + songName, 5)
                if (noteSequence.isEmpty())
                    snackbarMessage(player, "Song not found: " + "songs" + File.separator + songName)
                else
                    snackbarMessage(player, "Song Found: " + "songs" + File.separator + songName)
                lastNote = System.currentTimeMillis()
                elapsed = 0
            }
            NotebotMode.PLAY -> {
                if (!player.isCreative) {
                    while (noteSequence.isNotEmpty() && noteSequence.firstKey() <= elapsed) {
                        playNotes(noteSequence.pollFirstEntry().value, player, world)
                    }
                    elapsed += System.currentTimeMillis() - lastNote
                    lastNote = System.currentTimeMillis()
                } else snackbarMessage(player, "You are in creative mode and cannot play music.")
            }
        }
    })

    private fun playNotes(notes: ArrayList<Note>, player: ClientPlayerEntity, world: ClientWorld) {
        val blockPosArr: ArrayList<BlockPos?> = ArrayList()
        notes.forEach { n ->
            channelsArray = arrayOf(ChannelZero, ChannelOne, ChannelTwo, ChannelThree, ChannelFour)
            val number = (n.track % (channelsArray.size - 1))
            val enum = channelsArray[if (number < 0) 0 else number]
            snackbarMessage(player, enum.toString())
            blockPosArr.add(map[enum][if (enum == Instrument.HAT && hatAlwaysAsFSharp) 0 else n.notebotNote])
        }
        playBlock(blockPosArr, player, world)
    }

    fun snackbarMessage(player: ClientPlayerEntity, t: String) {
        player.sendMessage(text { +t }, true)
    }

    private fun playBlock(blockPosTracks: ArrayList<BlockPos?>, player: ClientPlayerEntity, world: ClientWorld) {
        if (blockPosTracks.isNotEmpty()) {
            blockPosTracks.forEach { blockPos ->
                if (blockPos != null) {
                    getIrreplaceableNeighbour(world, blockPos)?.let { (_, direction) ->
                        mc.networkHandler?.sendPacket(
                            PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, blockPos, direction)
                        )
                        mc.networkHandler?.sendPacket(
                            PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, blockPos, direction)
                        )
                        player.swingHand(Hand.MAIN_HAND)
                        (mc as IMinecraftClient).setItemUseCooldown(4)
                    }
                }
            }
        }
    }
}

