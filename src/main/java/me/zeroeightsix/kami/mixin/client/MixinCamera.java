package me.zeroeightsix.kami.mixin.client;

import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.event.CameraUpdateEvent;
import me.zeroeightsix.kami.feature.module.render.ModuleCamera;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class MixinCamera {

    @Shadow protected abstract double clipToSpace(double desiredCameraDistance);

    @Redirect(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;clipToSpace(D)D"))
    private double redirectClipToSpace(Camera camera, double desiredCameraDistance) {
        if (ModuleCamera.INSTANCE.getEnabled()) {
            desiredCameraDistance = ModuleCamera.INSTANCE.getDesiredDistance();
            if (ModuleCamera.INSTANCE.getClip()) return desiredCameraDistance;
        }
        return clipToSpace(desiredCameraDistance);
    }

    @Inject(method = "update", at = @At("TAIL"))
    public void onUpdate(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
        CameraUpdateEvent event = new CameraUpdateEvent((Camera) (Object) this, area, focusedEntity, thirdPerson, inverseView, tickDelta);
        KamiMod.EVENT_BUS.post(event);
    }

}
