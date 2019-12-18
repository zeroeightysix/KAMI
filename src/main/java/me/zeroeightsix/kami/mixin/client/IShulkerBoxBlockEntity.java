package me.zeroeightsix.kami.mixin.client;

import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.container.Container;
import net.minecraft.entity.player.PlayerInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ShulkerBoxBlockEntity.class)
public interface IShulkerBoxBlockEntity {

    @Invoker
    Container invokeCreateContainer(int i, PlayerInventory playerInventory);

}
