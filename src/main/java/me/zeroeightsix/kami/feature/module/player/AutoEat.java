package me.zeroeightsix.kami.feature.module.player;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.events.TickEvent;
import me.zeroeightsix.kami.mixin.client.IKeyBinding;
import me.zeroeightsix.kami.mixin.client.IMinecraftClient;
import me.zeroeightsix.kami.feature.module.Module;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

import java.util.Objects;

/**
 * Created by 086 on 8/04/2018.
 */
@Module.Info(name = "AutoEat", description = "Automatically eat when hungry", category = Module.Category.PLAYER)
public class AutoEat extends Module {

    private int lastSlot = -1;
    private boolean eating = false;

    private boolean isValid(ItemStack stack, int food) {
        return stack.getItem().getGroup() == ItemGroup.FOOD && (20 - food) >= Objects.requireNonNull(stack.getItem().getFoodComponent()).getHunger();
    }

    @EventHandler
    private Listener<TickEvent.Client.InGame> updateListener = new Listener<>(event -> {
        if (eating && !mc.player.isUsingItem()) {
            if (lastSlot != -1) {
                mc.player.inventory.selectedSlot = lastSlot;
                lastSlot = -1;
            }
            eating = false;
            KeyBinding.setKeyPressed(((IKeyBinding) mc.options.keyUse).getBoundKey(), true);
            return;
        }
        if (eating) return;

        HungerManager stats = mc.player.getHungerManager();
        if (isValid(mc.player.getOffHandStack(), stats.getFoodLevel())) {
            mc.player.setCurrentHand(Hand.OFF_HAND);
            eating = true;
            KeyBinding.setKeyPressed(((IKeyBinding) mc.options.keyUse).getBoundKey(), true);
            ((IMinecraftClient) mc).callDoAttack();
        } else {
            for (int i = 0; i < 9; i++) {
                if (isValid(mc.player.inventory.getStack(i), stats.getFoodLevel())) {
                    lastSlot = mc.player.inventory.selectedSlot;
                    mc.player.inventory.selectedSlot = i;
                    eating = true;
                    KeyBinding.setKeyPressed(((IKeyBinding) mc.options.keyUse).getBoundKey(), true);
                    ((IMinecraftClient) mc).callDoAttack();
                    return;
                }
            }
        }
    });

}
