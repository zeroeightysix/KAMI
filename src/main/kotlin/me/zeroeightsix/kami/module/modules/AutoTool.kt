package me.zeroeightsix.kami.module.modules.misc;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.events.PlayerAttackBlockEvent;
import me.zeroeightsix.kami.event.events.PlayerAttackEntityEvent;
import me.zeroeightsix.kami.mixin.client.IClientPlayerInteractionManager;
import me.zeroeightsix.kami.mixin.client.IMiningToolItem;
import me.zeroeightsix.kami.module.Module;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EntityGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MiningToolItem;
import net.minecraft.item.SwordItem;

/**
 * Created by 086 on 2/10/2018.
 */
@Module.Info(name = "AutoTool", description = "Automatically switch to the best tools when mining or attacking", category = Module.Category.MISC)
public class AutoTool extends Module {

    @EventHandler
    private Listener<PlayerAttackBlockEvent> leftClickListener = new Listener<>(event -> {
        equipBestTool(mc.world.getBlockState(event.getPosition()));
    });

    @EventHandler
    private Listener<PlayerAttackEntityEvent> attackListener = new Listener<>(event -> {
        equipBestWeapon();
    });

    private void equipBestTool(BlockState blockState) {
        int bestSlot = -1;
        double max = 0;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.inventory.getInvStack(i);
            if (stack.isEmpty()) continue;
            float speed = stack.getMiningSpeed(blockState);
            int eff;
            if (speed > 1) {
                speed += ((eff = EnchantmentHelper.getLevel(Enchantments.EFFICIENCY, stack)) > 0 ? (Math.pow(eff, 2) + 1) : 0);
                if (speed > max) {
                    max = speed;
                    bestSlot = i;
                }
            }
        }
        if (bestSlot != -1) equip(bestSlot);
    }

    public static void equipBestWeapon() {
        int bestSlot = -1;
        double maxDamage = 0;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.inventory.getInvStack(i);
            if (stack.isEmpty()) continue;
            if (stack.getItem() instanceof MiningToolItem || stack.getItem() instanceof SwordItem) {
                double damage = (((IMiningToolItem) stack.getItem()).getAttackDamage() + (double) EnchantmentHelper.getAttackDamage(stack, EntityGroup.DEFAULT));
                if (damage > maxDamage) {
                    maxDamage = damage;
                    bestSlot = i;
                }
            }
        }
        if (bestSlot != -1) equip(bestSlot);
    }

    private static void equip(int slot) {
        mc.player.inventory.selectedSlot = slot;
        ((IClientPlayerInteractionManager) mc.interactionManager).invokeSyncSelectedSlot();
    }

}
