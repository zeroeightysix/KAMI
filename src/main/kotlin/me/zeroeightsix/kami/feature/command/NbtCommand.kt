package me.zeroeightsix.kami.feature.command

import com.mojang.brigadier.CommandDispatcher
import glm_.vec2.Vec2
import glm_.vec4.Vec4
import imgui.Col
import imgui.ImGui
import imgui.TreeNodeFlag
import imgui.dsl.child
import imgui.dsl.window
import imgui.dsl.withStyleColor
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.event.TickEvent
import me.zeroeightsix.kami.gui.ImGuiScreen
import me.zeroeightsix.kami.gui.KamiHud
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
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.BlockPos

object NbtCommand : Command() {
    var screen = NbtScreen(CompoundTag())
    var open = true

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
                                Error.NO_BLOCK_ENTITY.send(it.source)
                                null
                            } else entity.toTag(CompoundTag())
                        }
                        HitResult.Type.ENTITY -> mc.targetedEntity?.toTag(CompoundTag())
                        else -> {
                            Error.NO_TARGET.send(it.source)
                            null
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
                        Error.NO_ITEM.send(it.source)
                        return@does 1
                    } else {
                        val tag = stack?.tag
                            ?: run { Error.NO_ITEM_NBT.send(it.source); return@does 1 }
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

    private enum class Error(val message: Text) {
        NO_TARGET(text(Formatting.RED) { +"No Target found!" }),
        NO_BLOCK_ENTITY(text(Formatting.RED) { +"This Block is not a BlockEntity!" }),
        NO_ITEM(text(Formatting.RED) { +"You must hold an item!" }),
        NO_ITEM_NBT(text(Formatting.RED) { +"The item you are holding has no NBT!" });

        fun send(source: CommandSource) {
            source replyWith message as MutableText
        }
    }
}

class NbtScreen(
    var tag: Tag,
    val defaultColor: Vec4 = Vec4(1f, 1f, 1f, 1f)
) : ImGuiScreen(text(null, "Kami NBT")) {
    override fun render(matrices: MatrixStack?, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(matrices, mouseX, mouseY, delta)

        KamiHud.frame(matrices!!) {
            this()
        }
    }

    operator fun invoke() = with(ImGui) {
        window("NBT") {
            if (button("Copy", Vec2(windowContentRegionWidth, 0)))
                copyTagToClipboard(tag)
            child("##nbtPane", border = true) {
                columns(2)
                showTree("NBT", tag)
                columns()
            }
        }
    }

    private fun showTree(tagName: String, tag: Tag, appendix: Int = 0): Unit = with(ImGui) {
        alignTextToFramePadding()
        var curApp = appendix
        if (treeNodeEx("$tagName##$curApp", TreeNodeFlag.NoTreePushOnOpen.i)) {
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
                                withStyleColor(Col.Text, tagColor(it)) {
                                    nbtLabelText(key, it.asString())
                                }
                        }
                }
                is AbstractListTag<*> -> {
                    for (t in tag)
                        if (tagIsDeep(t))
                            showTree("[array entry]", t, ++curApp)
                        else
                            withStyleColor(Col.Text, tagColor(t)) {
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
        is AbstractNumberTag -> Vec4(1f, 1f, 0f, 1f)
        is StringTag -> Vec4(0f, 1f, 0f, 1f)
        else -> defaultColor
    }

    private fun nbtLabelText(k: String, v: String) = with(ImGui) {
        bullet()
        text(k)
        nextColumn()
        text(v)
        nextColumn()
        separator()
    }
}
