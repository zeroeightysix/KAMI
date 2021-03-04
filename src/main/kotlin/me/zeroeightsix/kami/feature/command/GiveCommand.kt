package me.zeroeightsix.kami.feature.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import me.zeroeightsix.kami.mc
import net.minecraft.command.CommandSource
import net.minecraft.command.argument.ItemStackArgument
import net.minecraft.command.argument.ItemStackArgumentType
import net.minecraft.item.ItemStack
import net.minecraft.text.LiteralText

object GiveCommand : Command() {

    private val FAILED_EXCEPTION =
        DynamicCommandExceptionType { LiteralText(it.toString()) }

    override fun register(dispatcher: CommandDispatcher<CommandSource>) {
        dispatcher register rootLiteral("give") {
            argument("item", ItemStackArgumentType.itemStack()) {
                does { ctx ->
                    give("item" from ctx, 1)
                    0
                }

                integer("count") {
                    does { ctx ->
                        give("item" from ctx, "count" from ctx)
                        0
                    }
                }
            }
        }
    }

    private fun give(itemArgument: ItemStackArgument, count: Int) {
        if (!mc.player?.isCreative!!) {
            throw FAILED_EXCEPTION.create("You must be in creative mod to use this command")
        }

        val stack: ItemStack = itemArgument.createStack(count, false)

        if (mc.player?.mainHandStack?.isEmpty == true) {
            mc.interactionManager?.clickCreativeStack(stack, 36 + mc.player?.inventory?.selectedSlot!!)
        } else {
            val emptySlot: Int? = mc.player?.inventory?.emptySlot

            if (emptySlot!! < 9) {
                mc.interactionManager?.clickCreativeStack(stack, 36 + emptySlot)
            } else {
                mc.interactionManager?.clickCreativeStack(stack, 36 + mc.player?.inventory?.selectedSlot!!)
            }
        }
    }
}