package me.zeroeightsix.kami.feature.module

import imgui.dsl
import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting
import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.TickEvent
import me.zeroeightsix.kami.mixin.extend.itemUseCooldown
import me.zeroeightsix.kami.setting.ImGuiExtra
import me.zeroeightsix.kami.times
import me.zeroeightsix.kami.util.InstrumentMap
import me.zeroeightsix.kami.util.MidiParser
import me.zeroeightsix.kami.util.Note
import me.zeroeightsix.kami.util.Viewblock.getIrreplaceableNeighbour
import me.zeroeightsix.kami.util.plus
import me.zeroeightsix.kami.util.text
import me.zeroeightsix.kami.wrapDisabled
import net.minecraft.block.Blocks
import net.minecraft.block.NoteBlock.INSTRUMENT
import net.minecraft.block.NoteBlock.NOTE
import net.minecraft.block.enums.Instrument
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket
import net.minecraft.util.Formatting
import net.minecraft.util.Hand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import java.io.File
import java.io.IOException
import java.util.ArrayList
import java.util.TreeMap
import javax.sound.midi.InvalidMidiDataException

@Module.Info(
    name = "Notebot",
    category = Module.Category.MISC,
    description = "Plays music with noteblocks; put songs as .mid files in .minecraft/songs"
)
object Notebot : Module() {

    private var firstNote: Long = 0L
    private var elapsed: Long = 0L
    private var duration: Long = 0L
    private var playingSong = false

    private var noteSequence: TreeMap<Long, ArrayList<Note>> = TreeMap()
    private var instrumentMap = InstrumentMap()

    private var channelMap: TreeMap<Int, Instrument> = TreeMap()

    /* This is a bad way to have channels. In the future, I would like for there to be some way to display a map. */
    @Setting(name = "Channel 0")
    var channelZero = Instrument.HAT

    @Setting(name = "Channel 1")
    var channelOne = Instrument.HAT

    @Setting(name = "Channel 2")
    var channelTwo = Instrument.HAT

    @Setting(name = "Channel 3")
    var channelThree = Instrument.HAT

    @Setting(name = "Channel 4")
    var channelFour = Instrument.HAT

    @Setting
    var hatAlwaysAsFSharp = true

    @Setting(comment = "Name of MIDI file in the songs folder of your .minecraft directory")
    @ImGuiExtra.Post("renderButtons")
    var songName = "song_name.mid"

    @Suppress("unused")
    fun renderButtons() {
        dsl.button("Discover") { discoverSong() }
        dsl.button("Load") { loadSong() }
        playButton()
        pauseButton()
    }

    private fun playButton() {
        wrapDisabled(playingSong) {
            dsl.button("Play") {
                firstNote = System.currentTimeMillis() - elapsed
                playingSong = true
            }
        }
    }

    private fun pauseButton() {
        wrapDisabled(!playingSong) {
            dsl.button("Pause") {
                playingSong = false
            }
        }
    }

    private fun discoverSong() {
        val player = mc.player ?: return
        for (x in -4..4) {
            for (y in -4..4) {
                for (z in -4..4) {
                    val pos = player.pos?.plus(Vec3d(x.toDouble(), y.toDouble(), z.toDouble())) ?: break
                    if (player.world.getBlockState(BlockPos(pos))?.block == Blocks.NOTE_BLOCK) {
                        val noteBlock = player.world.getBlockState(BlockPos(pos))
                        instrumentMap.add(noteBlock.get(INSTRUMENT), noteBlock.get(NOTE), BlockPos(pos))
                    }
                }
            }
        }
    }

    private fun loadSong() {
        mc.player?.let {
            playingSong = false
            val path = "songs${File.separator}$songName"
            try {
                duration = 0
                MidiParser.parse(path).let { parsed ->
                    noteSequence = parsed.first
                    channelMap = parsed.second
                    duration = parsed.first.lastKey()
                }
                displayActionbar(it, "Loaded song $path")
                elapsed = 0
            } catch (e: IOException) {
                displayActionbar(it, "Sound not found $path")
            } catch (e: InvalidMidiDataException) {
                displayActionbar(it, "Invalid MIDI Data: $path")
            }
        }
    }

    @EventHandler
    val listener = Listener<TickEvent.InGame>({
        if (playingSong) {
            val player = it.player
            if (!player.isCreative) {
                while (noteSequence.isNotEmpty() && noteSequence.firstKey() <= elapsed) {
                    playNotes(noteSequence.pollFirstEntry().value, player, it.world)
                }
                if (noteSequence.isEmpty()) {
                    displayActionbar(player, "Finished playing song.")
                    playingSong = false
                }
                elapsed = System.currentTimeMillis() - firstNote
                updateSongProgress(player, elapsed, duration)
            } else {
                // Pause song
                playingSong = false
                displayActionbar(player, "You are in creative mode and cannot play music.")
            }
        }
    })

    private fun playNotes(notes: List<Note>, player: ClientPlayerEntity, world: World) {
        val blockPosArr = mutableListOf<BlockPos?>()
        notes.forEach { n ->
            val channelsArray: Array<Instrument> =
                arrayOf(channelZero, channelOne, channelTwo, channelThree, channelFour)
            val number = n.track % (channelsArray.size - 1)
            val instrument = channelsArray[if (number < 0) 0 else number]
            blockPosArr.add(instrumentMap[instrument][if (instrument == Instrument.HAT && hatAlwaysAsFSharp) 0 else n.notebotNote])
        }
        playBlocks(blockPosArr, player, world)
    }

    private fun playBlocks(blockPosTracks: List<BlockPos?>, player: ClientPlayerEntity, world: World) {
        blockPosTracks.asSequence().filterNotNull().forEach { blockPos ->
            getIrreplaceableNeighbour(world, blockPos)?.let { (_, direction) ->
                if (player.pos.isInRange(
                        Vec3d(blockPos.x.toDouble(), blockPos.y.toDouble(), blockPos.z.toDouble()),
                        6.0
                    )
                ) {
                    mc.networkHandler?.let {
                        it.sendPacket(
                            PlayerActionC2SPacket(
                                PlayerActionC2SPacket.Action.START_DESTROY_BLOCK,
                                blockPos,
                                direction
                            )
                        )
                        it.sendPacket(
                            PlayerActionC2SPacket(
                                PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK,
                                blockPos,
                                direction
                            )
                        )
                    }
                    player.swingHand(Hand.MAIN_HAND)
                    mc.itemUseCooldown = 4
                } else {
                    // Pause song
                    playingSong = false
                    displayActionbar(
                        player,
                        "You are not in range to play this block. Coordinates: [${blockPos.x}, ${blockPos.y}, ${blockPos.z}]"
                    )
                }
            }
        }
    }

    private fun updateSongProgress(player: ClientPlayerEntity, elapsed: Long, duration: Long) {
        val songbarLength = 32
        val elapsedSection = if (duration < 1) songbarLength - 1 else (elapsed.toInt() / (duration.toInt() / songbarLength).coerceAtLeast(1))
        val unplayedSection = songbarLength - (elapsedSection + 1)
        player.sendMessage(
            text {
                +"$songName: ${elapsed / 1000}s "
                +("-" * elapsedSection)(Formatting.RED)
                +"o"(Formatting.WHITE)
                +("-" * unplayedSection)(Formatting.WHITE)
                +" ${duration / 1000}s"
            },
            true
        )
    }

    private fun displayActionbar(player: ClientPlayerEntity, t: String) {
        player.sendMessage(text { +t }, true)
    }
}
