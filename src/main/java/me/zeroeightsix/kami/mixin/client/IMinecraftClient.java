package me.zeroeightsix.kami.mixin.client;

import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MinecraftClient.class)
public interface IMinecraftClient {

    @Accessor
    void setItemUseCooldown(int itemUseCooldown);

    @Accessor
    int getItemUseCooldown();

    @Invoker
    void callDoAttack();

    @Invoker
    void callDoItemUse();

    @Invoker
    void callOpenChatScreen(String text);

    @Accessor
    static int getCurrentFps() {
        throw new UnsupportedOperationException("Untransformed mixin!");
    }

}
