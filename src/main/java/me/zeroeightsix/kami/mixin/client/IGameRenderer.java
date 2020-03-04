package me.zeroeightsix.kami.mixin.client;

import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GameRenderer.class)
public interface IGameRenderer {

    @Invoker
    void invokeApplyCameraTransformations(float tickDelta);

}
