package me.zeroeightsix.kami.feature.module

import imgui.ImGui
import imgui.StyleVar
import imgui.dsl
import imgui.internal.sections.ItemFlag
import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting
import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.conditionalWrap
import me.zeroeightsix.kami.event.TickEvent
import me.zeroeightsix.kami.mixin.extend.setCooldown
import me.zeroeightsix.kami.setting.ImGuiExtra
import me.zeroeightsix.kami.util.InstrumentMap
import me.zeroeightsix.kami.util.MidiParser
import me.zeroeightsix.kami.util.Note
import me.zeroeightsix.kami.util.Viewblock.getIrreplaceableNeighbour
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

    private var lastNote: Long = 0L
    private var elapsed: Long = 0L
    var playingSong = false

    private var noteSequence: TreeMap<Long, ArrayList<Note>> = TreeMap()
    var instrumentMap = InstrumentMap()

    var channelMap: TreeMap<Int, Instrument> = TreeMap()


    /* This is a bad way to have channels. In the future, I would like for there to be some way to display a map.*/
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
    var songName = "tetris_theme.mid"

    fun renderButtons() {
        dsl.button("Discover") {
            discoverSong()
        }
        dsl.button("Load") {
            loadSong()
        }

        ImGui.pushItemFlag(ItemFlag.Disabled.i, true)
        ImGui.pushStyleVar(StyleVar.Alpha, ImGui.style.alpha * 0.5f)

        ImGui.popItemFlag()
        ImGui.popStyleVar()
        playButton()
        pauseButton()
    }

    private fun playButton() {
        (playingSong).conditionalWrap(
            {
                ImGui.pushItemFlag(ItemFlag.Disabled.i, true)
                ImGui.pushStyleVar(StyleVar.Alpha, ImGui.style.alpha * 0.5f)
            },
            {
                dsl.button("Play") {
                    playingSong = true
                }
            },
            {
                ImGui.popItemFlag()
                ImGui.popStyleVar()
            }
        )
    }
    private fun pauseButton() {
        (!playingSong).conditionalWrap(
            {
                ImGui.pushItemFlag(ItemFlag.Disabled.i, true)
                ImGui.pushStyleVar(StyleVar.Alpha, ImGui.style.alpha * 0.5f)
            },
            {
                dsl.button("Pause") {
                    playingSong = false
                }
            },
            {
                ImGui.popItemFlag()
                ImGui.popStyleVar()
            }
        )
    }

    private fun discoverSong() {
        mc.player?.let {
            for (x in -4..4) {
                for (y in -4..4) {
                    for (z in -4..4) {
                        val pos = it.pos?.plus(Vec3d(x.toDouble(), y.toDouble(), z.toDouble())) ?: break
                        if (it.world.getBlockState(BlockPos(pos))?.block == Blocks.NOTE_BLOCK) {
                            val noteBlock = it.world.getBlockState(BlockPos(pos))
                            snackbarMessage(
                                it, "" + noteBlock.get(INSTRUMENT) + "[" + noteBlock.get(NOTE) + "]"
                            )
                            instrumentMap.add(noteBlock.get(INSTRUMENT), noteBlock.get(NOTE), BlockPos(pos))
                        }
                    }
                }
            }
        }
    }
    private fun loadSong() {
        mc.player?.let {
            playingSong = false
            MidiParser.parse("songs${File.separator}$songName").let {
                noteSequence = it.first
                channelMap = it.second
            }
            if (noteSequence.isEmpty())
                snackbarMessage(it, "Unable to find MIDI: songs${File.separator}$songName")
            else
                snackbarMessage(it, "Loaded song ${File.separator}$songName")
        }
        lastNote = System.currentTimeMillis()
        elapsed = 0
    }

    @EventHandler
    val listener = Listener<TickEvent.InGame>({
        if (playingSong) {
            val player = it.player
            val world = it.world
            if (!player.isCreative) {
                while (noteSequence.isNotEmpty() && noteSequence.firstKey() <= elapsed) {
                    playNotes(noteSequence.pollFirstEntry().value, player, world)
                }
                if (noteSequence.isEmpty()) playingSong = false
                elapsed += System.currentTimeMillis() - lastNote
            } else snackbarMessage(player, "You are in creative mode and cannot play music.")
        }
        lastNote = System.currentTimeMillis()
    })

    private fun playNotes(notes: List<Note>, player: ClientPlayerEntity, world: ClientWorld) {
        val blockPosArr: ArrayList<BlockPos?> = ArrayList()
        notes.forEach { n ->
            val channelsArray: Array<Instrument> =
                arrayOf(channelZero, channelOne, channelTwo, channelThree, channelFour)
            val number = (n.track % (channelsArray.size - 1))
            val enum = channelsArray[if (number < 0) 0 else number]
            snackbarMessage(player, enum.toString())
            blockPosArr.add(instrumentMap[enum][if (enum == Instrument.HAT && hatAlwaysAsFSharp) 0 else n.notebotNote])
        }
        playBlock(blockPosArr, player, world)
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

                        mc.setCooldown(4)
                    }
                }
            }
        }
    }

    fun snackbarMessage(player: ClientPlayerEntity, t: String) {
        player.sendMessage(text { +t }, true)
    }
}

