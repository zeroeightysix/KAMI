package me.zeroeightsix.kami.feature.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listenable
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.event.events.TickEvent
import me.zeroeightsix.kami.mixin.client.IShulkerBoxBlockEntity
import me.zeroeightsix.kami.util.ShulkerBoxCommon
import me.zeroeightsix.kami.util.Wrapper
import net.minecraft.block.ShulkerBoxBlock
import net.minecraft.block.entity.ShulkerBoxBlockEntity
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.ingame.ShulkerBoxScreen
import net.minecraft.container.ShulkerBoxContainer
import net.minecraft.item.BlockItem
import net.minecraft.server.command.CommandSource
import net.minecraft.text.LiteralText
import java.util.function.Function

/**
 * @author 086
 */
object PeekCommand : Command(), Listenable {
    var sb: ShulkerBoxBlockEntity? = null

    private val FAILED_EXCEPTION =
        DynamicCommandExceptionType(Function { o: Any ->
            LiteralText(o.toString())
        })

    override fun register(dispatcher: CommandDispatcher<CommandSource>) {
        dispatcher.register(
            LiteralArgumentBuilder.literal<CommandSource>("peek").executes { context: CommandContext<CommandSource>? ->
            val stack = Wrapper.getPlayer().inventory.mainHandStack
            if (ShulkerBoxCommon.isShulkerBox(stack.item)) {
                val entityBox =
                    ShulkerBoxBlockEntity(((stack.item as BlockItem).block as ShulkerBoxBlock).color)
                entityBox.world = Wrapper.getWorld()
                val tag = stack.getSubTag("BlockEntityTag")
                if (tag != null) {
                    entityBox.fromTag(tag)
                    sb = entityBox
                    KamiMod.EVENT_BUS.subscribe(this)
                } else {
                    throw FAILED_EXCEPTION.create("Couldn't peek into shulker box. It might be empty.")
                }
            } else {
                throw FAILED_EXCEPTION.create("You must be holding a shulker box to peek into.")
            }
            0
        })
    }

    @EventHandler
    var tickListener = Listener(
        EventHook<TickEvent.Client.InGame> {
            if (sb != null) {
                val container = (sb as IShulkerBoxBlockEntity?)!!.invokeCreateContainer(
                    -1,
                    Wrapper.getPlayer().inventory
                ) as ShulkerBoxContainer
                val gui = ShulkerBoxScreen(
                    container,
                    Wrapper.getPlayer().inventory,
                    sb!!.displayName
                )
                MinecraftClient.getInstance().openScreen(gui)
                sb = null
                KamiMod.EVENT_BUS.unsubscribe(this)
            }
        }
    )
}