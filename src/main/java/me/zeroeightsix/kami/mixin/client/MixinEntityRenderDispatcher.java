package me.zeroeightsix.kami.mixin.client;

import me.zeroeightsix.kami.mixin.duck.HasRenderPosition;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(EntityRenderDispatcher.class)
public abstract class MixinEntityRenderDispatcher implements HasRenderPosition {

    @Shadow
    private double renderPosX;
    @Shadow
    private double renderPosY;
    @Shadow
    private double renderPosZ;

    public double getRenderPosX() {
        return renderPosX;
    }

    public double getRenderPosY() {
        return renderPosY;
    }

    public double getRenderPosZ() {
        return renderPosZ;
    }

}
