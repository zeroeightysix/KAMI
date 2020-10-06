package me.zeroeightsix.kami.feature.module

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.TickEvent.InGame
import me.zeroeightsix.kami.mixin.client.IKeyBinding
import net.minecraft.client.options.KeyBinding
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemStack
import net.minecraft.util.Hand

@Module.Info(name = "AutoEat", description = "Automatically eat when hungry", category = Module.Category.PLAYER)
object AutoEat : Module() {
    private var eating: Hand? = null
    private var oldSlot: Int? = null

    private fun isValid(stack: ItemStack, food: Int): Boolean {
        return stack.item.group === ItemGroup.FOOD && 20 - food >= stack.item?.foodComponent?.hunger ?: 0
    }

    @EventHandler
    private val updateListener = Listener<InGame>({ it ->
        val player = it.player
        val foodLevel = player.hungerManager.foodLevel

        eating?.let { hand ->
            // Set the use keybinding to true. This is so minecraft doesn't try to cancel the eating action because the key is 'no longer' held down.
            KeyBinding.setKeyPressed((mc.options.keyUse as IKeyBinding).boundKey, true)
            mc.interactionManager?.interactItem(player, mc.world, hand)
            // If the current item isn't a valid food item, quit.
            // Usually happens when it is consumed.
            if (!isValid(player.inventory.getStack(player.inventory.selectedSlot), foodLevel)) {
                // Stop trying to eat from this hand
                eating = null
                // Revert the key use binding to false.
                KeyBinding.setKeyPressed((mc.options.keyUse as IKeyBinding).boundKey, false)
                oldSlot?.let {
                    // If we had an oldSlot (nonnull if AutoEat modified the selected slot), revert to it
                    player.inventory.selectedSlot = it
                    oldSlot = null
                }
            }
            return@Listener
        }
        if (player.isUsingItem) return@Listener

        if (isValid(player.offHandStack, foodLevel)) eating = Hand.OFF_HAND
        else {
            // No food in offhand, let's search the hotbar
            (0..9).forEach { slot ->
                if (isValid(player.inventory.getStack(slot), foodLevel)) {
                    eating = Hand.MAIN_HAND
                    oldSlot = player.inventory.selectedSlot
                    player.inventory.selectedSlot = slot
                    return@Listener
                }
            }
        }
    })
}
