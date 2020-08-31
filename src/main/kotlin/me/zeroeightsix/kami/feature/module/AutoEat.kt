package me.zeroeightsix.kami.feature.module

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.TickEvent.Client.InGame
import me.zeroeightsix.kami.mixin.client.IKeyBinding
import net.minecraft.client.options.KeyBinding
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemStack
import net.minecraft.util.Hand

@Module.Info(name = "AutoEat", description = "Automatically eat when hungry", category = Module.Category.PLAYER)
object AutoEat : Module() {
    var eating: Hand? = null

    private fun isValid(stack: ItemStack, food: Int): Boolean {
        return stack.item.group === ItemGroup.FOOD && 20 - food >= stack.item?.foodComponent?.hunger ?: 0
    }

    @EventHandler
    private val updateListener = Listener<InGame>({
        val player =
            mc.player ?: return@Listener // InGame gets fired if player != null so this return shouldn't ever happen

        val foodLevel = player.hungerManager.foodLevel

        eating?.let {
            // If the current item isn't a valid food item, quit.
            // If it is, try to eat it. If it is consumed, quit.
            KeyBinding.setKeyPressed((mc.options.keyUse as IKeyBinding).boundKey, true)
            mc.interactionManager?.interactItem(player, mc.world, it)
            if (!isValid(player.inventory.getStack(player.inventory.selectedSlot), foodLevel)) {
                eating = null // Reset the eating hand
                KeyBinding.setKeyPressed((mc.options.keyUse as IKeyBinding).boundKey, false)
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
                    player.inventory.selectedSlot = slot
                    return@Listener
                }
            }
        }
    })
}
