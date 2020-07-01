package me.zeroeightsix.kami.mixin.client;

import me.zeroeightsix.kami.feature.module.render.ModuleCamera;
import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Camera.class)
public abstract class MixinCamera {

    @Shadow protected abstract double clipToSpace(double desiredCameraDistance);

    @Redirect(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;clipToSpace(D)D"))
    private double redirectClipToSpace(Camera camera, double desiredCameraDistance) {
        if (ModuleCamera.INSTANCE.isEnabled()) {
            desiredCameraDistance = ModuleCamera.INSTANCE.getDesiredDistance();
            if (ModuleCamera.INSTANCE.getClip()) return desiredCameraDistance;
        }
        return clipToSpace(desiredCameraDistance);
    }

}
