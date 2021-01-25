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
import me.zeroeightsix.kami.gui.ImGuiScreen
import me.zeroeightsix.kami.gui.ImguiDSL.button
import me.zeroeightsix.kami.gui.ImguiDSL.child
import me.zeroeightsix.kami.gui.ImguiDSL.window
import me.zeroeightsix.kami.gui.ImguiDSL.windowContentRegionWidth
import me.zeroeightsix.kami.gui.ImguiDSL.withStyleColor
import me.zeroeightsix.kami.gui.KamiImgui
import me.zeroeightsix.kami.mc
import me.zeroeightsix.kami.util.text
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.command.CommandSource
import net.minecraft.nbt.AbstractListTag
import net.minecraft.nbt.AbstractNumberTag
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.StringTag
import net.minecraft.nbt.Tag
import net.minecraft.text.LiteralText
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.BlockPos

object NbtCommand : Command() {
    var screen = NbtScreen(CompoundTag())
    var open = true

    private val FAILED_EXCEPTION =
        DynamicCommandExceptionType { LiteralText(it.toString()) }

    // this is required, as the chat closes all screens when a command is entered,
    // so the screen needs to be opened a tick later
    var opener: Listener<TickEvent.InGame>? = null

    init {
        opener = Listener({
            mc.openScreen(screen)
            KamiMod.EVENT_BUS.unsubscribe(opener)
        })
    }

    override fun register(dispatcher: CommandDispatcher<CommandSource>) {
        dispatcher register rootLiteral("nbt") {
            literal("look") {
                does {
                    val target = mc.crosshairTarget
                    val tag = when (target?.type) {
                        HitResult.Type.BLOCK -> {
                            val entity = mc.world?.getBlockEntity(BlockPos(target.pos))
                            if (entity == null) {
                                throw FAILED_EXCEPTION.create("This Block is not a BlockEntity!")
                            } else entity.toTag(CompoundTag())
                        }
                        HitResult.Type.ENTITY -> mc.targetedEntity?.toTag(CompoundTag())
                        else -> {
                            throw FAILED_EXCEPTION.create("No Target found!")
                        }
                    }

                    open(tag ?: return@does 1)
                    0
                }
            }

            literal("self") {
                does {
                    open(mc.player?.toTag(CompoundTag()) ?: return@does 1)
                    0
                }
            }

            literal("hand") {
                does {
                    val stack = mc.player?.mainHandStack
                    if (stack?.isEmpty == true) {
                        throw FAILED_EXCEPTION.create("You must hold an item!")
                    } else {
                        val tag = stack?.tag
                            ?: run { throw FAILED_EXCEPTION.create("The item you are holding has no NBT!") }
                        open(tag)
                    }
                    0
                }
            }
        }
    }

    private fun open(tag: Tag) {
        screen.tag = tag
        KamiMod.EVENT_BUS.subscribe(opener)
    }
}

class NbtScreen(
    var tag: Tag,
    private val defaultColor: Int = 0xFFFFFFFF.toInt()
) : ImGuiScreen(text(null, "Kami NBT")) {
    override fun render(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(matrices, mouseX, mouseY, delta)

        KamiImgui.frame(matrices) {
            this()
        }
    }

    operator fun invoke() {
        window("NBT") {
            button("Copy", windowContentRegionWidth, 0f) {
                copyTagToClipboard(tag)
            }

            child("##nbtPane", border = true) {
                columns(2)
                showTree("NBT", tag)
                columns()
            }
        }
    }

    private fun showTree(tagName: String, tag: Tag, appendix: Int = 0) {
        alignTextToFramePadding()
        var curApp = appendix
        if (treeNodeEx("$tagName##$curApp", ImGuiTreeNodeFlags.NoTreePushOnOpen)) {
            nextColumn()
            nextColumn()
            treePush()
            when (tag) {
                is CompoundTag -> {
                    for (key in tag.keys)
                        tag[key]?.let {
                            if (tagIsDeep(it))
                                showTree(key, it, ++curApp)
                            else
                                withStyleColor(ImGuiCol.Text, tagColor(it)) {
                                    nbtLabelText(key, it.asString())
                                }
                        }
                }
                is AbstractListTag<*> -> {
                    for (t in tag)
                        if (tagIsDeep(t))
                            showTree("[array entry]", t, ++curApp)
                        else
                            withStyleColor(ImGuiCol.Text, tagColor(t)) {
                                nbtLabelText("[array entry]", t.asString())
                            }
                }
                else -> {
                    textColored(tagColor(tag), tag.asString())
                    separator()
                }
            }
            treePop()
        }
        nextColumn()
        nextColumn()
    }

    private fun tagIsDeep(tag: Tag): Boolean {
        val test = { t: Tag -> t is CompoundTag || t is ListTag }
        return when (tag) {
            is CompoundTag -> tag.keys.size > 1 || tag.keys.map { tag[it]!! }.any(test)
            is AbstractListTag<*> -> tag.size > 1 || tag.any(test)
            else -> false
        }
    }

    private fun copyTagToClipboard(tag: Tag) {
        mc.keyboard.clipboard = tag.asString()
    }

    private fun tagColor(tag: Tag) = when (tag) {
        is AbstractNumberTag -> 0xFFFF00FF.toInt()
        is StringTag -> 0x00FF00FF.toInt()
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
