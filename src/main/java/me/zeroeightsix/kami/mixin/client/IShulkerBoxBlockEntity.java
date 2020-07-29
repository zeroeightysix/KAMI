package me.zeroeightsix.kami.mixin.client;

import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ShulkerBoxBlockEntity.class)
public interface IShulkerBoxBlockEntity {

    @Invoker
    ScreenHandler invokeCreateContainer(int i, PlayerInventory playerInventory);

}
