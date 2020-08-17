package me.zeroeightsix.kami.mixin.client;

import net.minecraft.client.render.Camera;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Camera.class)
public interface ICamera {

    @Invoker
    void callSetPos(Vec3d pos);

    @Invoker
    void callSetRotation(float yaw, float pitch);

}
