package me.zeroeightsix.kami.mixin.client;

import net.minecraft.client.render.entity.EntityRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityRenderDispatcher.class)
public interface IEntityRenderDispatcher {

    @Accessor
    double getRenderPosX();
    @Accessor
    double getRenderPosY();
    @Accessor
    double getRenderPosZ();

}
