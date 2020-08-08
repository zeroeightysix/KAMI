package me.zeroeightsix.kami.mixin.client;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Entity.class)
public interface IEntity {

    @Invoker("movementInputToVelocity")
    static Vec3d movementInputToVelocity(Vec3d movementInput, float speed, float yaw) {
        throw new UnsupportedOperationException("Untransformed mixin!");
    }

}
