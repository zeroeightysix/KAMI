package me.zeroeightsix.kami.mixin.client;

import com.mojang.blaze3d.platform.GlStateManager;
import me.zeroeightsix.kami.feature.module.EntitySpeed;
import me.zeroeightsix.kami.util.Wrapper;
import net.minecraft.client.render.entity.model.BoatEntityModel;
import net.minecraft.entity.vehicle.BoatEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Created by 086 on 15/12/2017.
 */
@Mixin(BoatEntityModel.class)
public class MixinBoatEntityModel {

    @Inject(method = "method_17071", at = @At("HEAD"))
    public void render(BoatEntity boatEntity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale, CallbackInfo info) {
        if (Wrapper.getPlayer().getVehicle() == boatEntity && EntitySpeed.INSTANCE.isEnabled()) {
            GlStateManager.color4f(1, 1, 1, EntitySpeed.INSTANCE.getOpacity());
            GlStateManager.enableBlend();
        }
    }

}
