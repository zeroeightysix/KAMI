package me.zeroeightsix.kami.feature.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import imgui.ImGui.alignTextToFramePadding
import imgui.ImGui.bullet
import imgui.ImGui.columns
import imgui.ImGui.nextColumn
import imgui.ImGui.separator
import imgui.ImGui.text
import imgui.ImGui.textColored
import imgui.ImGui.treeNodeEx
import imgui.ImGui.treePop
import imgui.ImGui.treePush
import imgui.flag.ImGuiCol
import imgui.flag.ImGuiTreeNodeFlags
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.event.TickEvent
import me.zeroeightsix.kami.feature.command.NbtCommand.open
import me.zeroeightsix.kami.gui.ImGuiScreen
import me.zeroeightsix.kami.gui.ImguiDSL.button
import me.zeroeightsix.kami.gui.ImguiDSL.child
import me.zeroeightsix.kami.gui.ImguiDSL.window
import me.zeroeightsix.kami.gui.ImguiDSL.windowContentRegionWidth
import me.zeroeightsix.kami.gui.ImguiDSL.withStyleColour
import me.zeroeightsix.kami.gui.KamiImgui
import me.zeroeightsix.kami.mc
import me.zeroeightsix.kami.util.text
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.command.CommandSource
import net.minecraft.nbt.AbstractNbtList
import net.minecraft.nbt.AbstractNbtNumber
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList
import net.minecraft.nbt.NbtString
import net.minecraft.nbt.NbtElement
import net.minecraft.text.LiteralText
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.BlockPos

object NbtCommand : Command() {
    var screen = NbtScreen(NbtCompound())
    var open = false

    private val FAILED_EXCEPTION =
        DynamicCommandExceptionType { LiteralText(it.toString()) }

    // this is required, as the chat closes all screens when a command is entered,
    // so the screen needs to be opened a tick later
    var opener: Listener<TickEvent.InGame>? = null

    init {
        opener = Listener({
            open = true
            mc.setScreen(screen)
            KamiMod.EVENT_BUS.unsubscribe(opener)
        })
    }

    override fun register(dispatcher: CommandDispatcher<CommandSource>) {
        dispatcher register rootLiteral("nbt") {
            literal("look") {
                does {
                    val target = mc.crosshairTarget
                    val NbtElement = when (target?.type) {
                        HitResult.Type.BLOCK -> {
                            val entity = mc.world?.getBlockEntity(BlockPos(target.pos))
                            if (entity == null) {
                                throw FAILED_EXCEPTION.create("This Block is not a BlockEntity!")
                            } else entity.toNbtElement(NbtCompound())
                        }
                        HitResult.Type.ENTITY -> mc.targetedEntity?.toNbtElement(NbtCompound())
                        else -> {
                            throw FAILED_EXCEPTION.create("No Target found!")
                        }
                    }

                    open(NbtElement ?: return@does 1)
                    0
                }
            }

            literal("self") {
                does {
                    open(mc.player?.toNbtElement(NbtCompound()) ?: return@does 1)
                    0
                }
            }

            literal("hand") {
                does {
                    val stack = mc.player?.mainHandStack
                    if (stack?.isEmpty == true) {
                        throw FAILED_EXCEPTION.create("You must hold an item!")
                    } else {
                        val NbtElement = stack?.NbtElement
                            ?: run { throw FAILED_EXCEPTION.create("The item you are holding has no NBT!") }
                        open(NbtElement)
                    }
                    0
                }
            }
        }
    }

    private fun open(NbtElement: NbtElement) {
        screen.NbtElement = NbtElement
        KamiMod.EVENT_BUS.subscribe(opener)
    }
}

class NbtScreen(
    var NbtElement: NbtElement,
    private val defaultColor: Int = 0xFFFFFFFF.toInt()
) : ImGuiScreen(text(null, "Kami NBT")) {
    override fun render(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(matrices, mouseX, mouseY, delta)

        KamiImgui.frame(matrices) {
            this()
        }
    }

    operator fun invoke() {
        window("NBT", NbtCommand::open) {
            button("Copy", windowContentRegionWidth, 0f) {
                copyNbtElementToClipboard(NbtElement)
            }

            child("##nbtPane", border = true) {
                columns(2)
                showTree("NBT", NbtElement)
                columns()
            }
        }
        if (!open) {
            this.onClose()
        }
    }

    private fun showTree(NbtElementName: String, NbtElement: NbtElement, appendix: Int = 0) {
        alignTextToFramePadding()
        var curApp = appendix
        if (treeNodeEx("$NbtElementName##$curApp", ImGuiTreeNodeFlags.NoTreePushOnOpen)) {
            nextColumn()
            nextColumn()
            treePush()
            when (NbtElement) {
                is NbtCompound -> {
                    for (key in NbtElement.keys)
                        NbtElement[key]?.let {
                            if (NbtElementIsDeep(it))
                                showTree(key, it, ++curApp)
                            else
                                withStyleColour(ImGuiCol.Text, NbtElementColor(it)) {
                                    nbtLabelText(key, it.asString())
                                }
                        }
                }
                is AbstractNbtList<*> -> {
                    for (t in NbtElement)
                        if (NbtElementIsDeep(t))
                            showTree("[array entry]", t, ++curApp)
                        else
                            withStyleColour(ImGuiCol.Text, NbtElementColor(t)) {
                                nbtLabelText("[array entry]", t.asString())
                            }
                }
                else -> {
                    textColored(NbtElementColor(NbtElement), NbtElement.asString())
                    separator()
                }
            }
            treePop()
        }
        nextColumn()
        nextColumn()
    }

    private fun NbtElementIsDeep(NbtElement: NbtElement): Boolean {
        val test = { t: NbtElement -> t is NbtCompound || t is NbtList }
        return when (NbtElement) {
            is NbtCompound -> NbtElement.keys.size > 1 || NbtElement.keys.map { NbtElement[it]!! }.any(test)
            is AbstractNbtList<*> -> NbtElement.size > 1 || NbtElement.any(test)
            else -> false
        }
    }

    private fun copyNbtElementToClipboard(NbtElement: NbtElement) {
        mc.keyboard.clipboard = NbtElement.asString()
    }

    private fun NbtElementColor(NbtElement: NbtElement) = when (NbtElement) {
        is AbstractNbtNumber -> 0xFFFF00FF.toInt()
        is NbtString -> 0x00FF00FF.toInt()
        else -> defaultColor
    }

    private fun nbtLabelText(k: String, v: String) {
        bullet()
        text(k)
        nextColumn()
        text(v)
        nextColumn()
        separator()
    }
}
