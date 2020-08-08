package me.zeroeightsix.kami.feature.module.player;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.events.TickEvent;
import me.zeroeightsix.kami.feature.module.Module;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

/**
 * Created by 086 on 24/01/2018.
 */
@Module.Info(name = "AutoArmour", category = Module.Category.PLAYER)
public class AutoArmour extends Module {

    @EventHandler
    private Listener<TickEvent.Client.InGame> updateListener = new Listener<>(event -> {
        if (mc.player.age % 2 == 0) return;
        // check screen
        if(mc.currentScreen instanceof GenericContainerScreen)
            return;

        // store slots and values of best armor pieces
        int[] bestArmorSlots = new int[4];
        int[] bestArmorValues = new int[4];

        // initialize with currently equipped armor
        for(int armorType = 0; armorType < 4; armorType++)
        {
            ItemStack oldArmor = mc.player.inventory.getArmorStack(armorType);

            if(oldArmor != null && oldArmor.getItem() instanceof ArmorItem)
                bestArmorValues[armorType] = ((ArmorItem) oldArmor.getItem()).getProtection();

            bestArmorSlots[armorType] = -1;
        }

        // search inventory for better armor
        for(int slot = 0; slot < 36; slot++)
        {
            ItemStack stack = mc.player.inventory.getStack(slot);

            if (stack.getCount() > 1)
                continue;

            if(stack == null || !(stack.getItem() instanceof ArmorItem))
                continue;

            ArmorItem armor = (ArmorItem)stack.getItem();
            int armorType = armor.getSlotType().ordinal() - 2;

            if (armorType == 2 && mc.player.inventory.getArmorStack(armorType).getItem().equals(Items.ELYTRA)) continue;

            int armorValue = armor.getProtection();

            if(armorValue > bestArmorValues[armorType])
            {
                bestArmorSlots[armorType] = slot;
                bestArmorValues[armorType] = armorValue;
            }
        }

        // equip better armor
        for(int armorType = 0; armorType < 4; armorType++)
        {
            // check if better armor was found
            int slot = bestArmorSlots[armorType];
            if(slot == -1)
                continue;

            // check if armor can be swapped
            // needs 1 free slot where it can put the old armor
            ItemStack oldArmor = mc.player.inventory.getArmorStack(armorType);
            if(oldArmor != ItemStack.EMPTY
                    || mc.player.inventory.getEmptySlot() != -1)
            {
                // hotbar fix
                if(slot < 9)
                    slot += 36;

                // swap armor
                mc.interactionManager.clickSlot(0, 8 - armorType, 0,
                        SlotActionType.QUICK_MOVE, mc.player);
                mc.interactionManager.clickSlot(0, slot, 0,
                        SlotActionType.QUICK_MOVE, mc.player);

                break;
            }
        }
    });

}
