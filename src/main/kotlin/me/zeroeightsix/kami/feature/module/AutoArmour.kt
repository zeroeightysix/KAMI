package me.zeroeightsix.kami.feature.module

import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting
import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.TickEvent.Client.InGame
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen
import net.minecraft.item.ArmorItem
import net.minecraft.item.ElytraItem
import net.minecraft.item.ItemStack
import net.minecraft.screen.slot.SlotActionType

@Module.Info(name = "AutoArmour", category = Module.Category.PLAYER)
object AutoArmour : Module() {

    @Setting
    var preferElytra = false

    @EventHandler
    private val updateListener = Listener<InGame>({
        val player = mc.player ?: return@Listener
        if (mc.player!!.age % 2 == 0) return@Listener
        // If a container is opened, don't try to move around items.
        if (mc.currentScreen is GenericContainerScreen) return@Listener

        // store slots and values of best armor pieces
        val bestArmorSlots = IntArray(4)
        val bestArmorValues = IntArray(4)

        // initialize with currently equipped armor
        run {
            var armorType = 0
            while (armorType < 4) {
                val oldArmor = player.inventory.getArmorStack(armorType)
                if (preferElytra && oldArmor.item is ElytraItem)
                    bestArmorValues[armorType] = Integer.MAX_VALUE
                if (oldArmor != null && oldArmor.item is ArmorItem) bestArmorValues[armorType] =
                    (oldArmor.item as ArmorItem).protection
                bestArmorSlots[armorType] = -1
                armorType++
            }
        }

        // search inventory for better armor
        var slot = 0
        while (slot < 36) {
            slot++
            val stack = player.inventory.getStack(slot) ?: continue
            if (stack.item is ElytraItem && preferElytra) {
                bestArmorSlots[2] = slot
                bestArmorValues[2] = Integer.MAX_VALUE
                continue
            }
            if (stack.count > 1 || stack.item !is ArmorItem) {
                continue
            }
            val armor = stack.item as ArmorItem
            val armorType = armor.slotType.ordinal - 2
            val armorValue = armor.protection
            if (armorValue > bestArmorValues[armorType]) {
                bestArmorSlots[armorType] = slot
                bestArmorValues[armorType] = armorValue
            }
        }

        // equip better armor
        var armorType = 0
        while (armorType < 4) {
            // check if better armor was found
            var slot = bestArmorSlots[armorType]
            if (slot == -1) {
                armorType++
                continue
            }

            // check if armor can be swapped
            // needs 1 free slot where it can put the old armor
            val oldArmor = player.inventory.getArmorStack(armorType)
            if (oldArmor != ItemStack.EMPTY ||
                player.inventory.emptySlot != -1
            ) {
                // hotbar fix
                if (slot < 9) slot += 36

                // swap armor
                mc.interactionManager!!.clickSlot(
                    0,
                    8 - armorType,
                    0,
                    SlotActionType.QUICK_MOVE,
                    mc.player
                )
                mc.interactionManager!!.clickSlot(
                    0,
                    slot,
                    0,
                    SlotActionType.QUICK_MOVE,
                    mc.player
                )
                break
            }
            armorType++
        }
    })
}
