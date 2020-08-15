package me.zeroeightsix.kami.mixin.client;

import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GameRenderer.class)
public interface IGameRenderer {

    @Accessor
    void setRenderHand(boolean renderHand);

}
