package me.zeroeightsix.kami.feature.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.Listenable
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.event.TickEvent
import me.zeroeightsix.kami.mc
import me.zeroeightsix.kami.mixin.client.IShulkerBoxBlockEntity
import me.zeroeightsix.kami.util.ShulkerBoxCommon
import me.zeroeightsix.kami.util.text
import net.minecraft.block.ShulkerBoxBlock
import net.minecraft.block.entity.ShulkerBoxBlockEntity
import net.minecraft.client.gui.screen.ingame.ShulkerBoxScreen
import net.minecraft.item.BlockItem
import net.minecraft.screen.ShulkerBoxScreenHandler
import net.minecraft.command.CommandSource
import net.minecraft.text.LiteralText
import net.minecraft.util.Formatting.RED

@Suppress("UNUSED")
object PeekCommand : Command(), Listenable {
    private var sb: ShulkerBoxBlockEntity? = null

    private val FAILED_EXCEPTION =
        DynamicCommandExceptionType { LiteralText(it.toString()) }

    override fun register(dispatcher: CommandDispatcher<CommandSource>) {
        dispatcher register rootLiteral("peek") {
            does {
                val stack = mc.player?.inventory?.mainHandStack
                if (ShulkerBoxCommon.isShulkerBox(stack?.item)) {
                    val entityBox =
                        ShulkerBoxBlockEntity(((stack?.item as BlockItem).block as ShulkerBoxBlock).color)
                    val tag = stack.getSubTag("BlockEntityTag")
                    val state = mc.world?.getBlockState(entityBox.pos)
                    if (tag != null && state != null) {
                        entityBox.fromTag(state, tag)
                        sb = entityBox
                        KamiMod.EVENT_BUS.subscribe(this@PeekCommand)
                    } else {
                        throw FAILED_EXCEPTION.create("Couldn't peek into shulker box. It might be empty.")
                    }
                } else {
                    throw FAILED_EXCEPTION.create("You must be holding a shulker box to peek into.")
                }
                0
            }
        }
    }

    @EventHandler
    var tickListener = Listener<TickEvent.InGame>({
        val player = it.player
        if (sb != null) {
            try {
                val container = (sb as IShulkerBoxBlockEntity?)!!.invokeCreateScreenHandler(
                    -1,
                    player?.inventory
                ) as ShulkerBoxScreenHandler
                val gui = ShulkerBoxScreen(
                    container,
                    player?.inventory,
                    sb!!.displayName
                )
                mc.openScreen(gui)
                sb = null
            } catch (e: Exception) {
                player?.sendMessage(
                    text(RED, "Failed to read shulker box contents."),
                    true
                )
            }
            KamiMod.EVENT_BUS.unsubscribe(this)
        }
    }
    )
}
