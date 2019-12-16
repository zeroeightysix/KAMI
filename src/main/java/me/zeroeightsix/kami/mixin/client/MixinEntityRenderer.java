package me.zeroeightsix.kami.mixin.client;

import net.minecraft.client.render.entity.EntityRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Created by 086 on 11/12/2017.
 */
@Mixin(EntityRenderDispatcher.class)
public class MixinEntityRenderer {

    private boolean nightVision = false;



}
