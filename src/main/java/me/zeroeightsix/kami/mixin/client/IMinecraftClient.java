package me.zeroeightsix.kami.mixin.client;

import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MinecraftClient.class)
public interface IMinecraftClient {

    @Accessor
    void setItemUseCooldown(int itemUseCooldown);

    @Invoker
    void callDoAttack();

    @Invoker
    void callDoItemUse();

}
