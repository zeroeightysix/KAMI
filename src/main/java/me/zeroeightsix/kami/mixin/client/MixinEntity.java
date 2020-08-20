package me.zeroeightsix.kami.mixin.client;

import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.event.EntityEvent;
import me.zeroeightsix.kami.event.EntityVelocityMultiplierEvent;
import me.zeroeightsix.kami.event.MoveEntityFluidEvent;
import me.zeroeightsix.kami.event.UpdateLookEvent;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class MixinEntity  {

    @Shadow public World world;

    @Redirect(method = "pushAwayFrom", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;addVelocity(DDD)V", ordinal = 0))
    public void addVelocity(Entity entity, double x, double y, double z) {
        EntityEvent.EntityCollision event = new EntityEvent.EntityCollision(entity, x, y, z);
        KamiMod.EVENT_BUS.post(event);
        if (event.isCancelled()) return;
        entity.addVelocity(event.getX(), event.getY(), event.getZ());
    }

    @Redirect(method = "updateMovementInFluid", at = @At(value = "INVOKE", target = "Lnet/minecraft/fluid/FluidState;getVelocity(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/util/math/Vec3d;"))
    public Vec3d getVelocity(FluidState fluidState, BlockView world, BlockPos pos) {
        Vec3d vec = fluidState.getVelocity(world, pos);
        MoveEntityFluidEvent event = new MoveEntityFluidEvent(((Entity) (Object) this), vec);
        KamiMod.EVENT_BUS.post(event);
        return event.isCancelled() ? Vec3d.ZERO : event.getMovement();
    }

    @Inject(method = "getVelocityMultiplier", at = @At("RETURN"), cancellable = true)
    public void onGetVelocityMultiplier(CallbackInfoReturnable<Float> cir) {
        float returnValue = cir.getReturnValue();
        EntityVelocityMultiplierEvent event = new EntityVelocityMultiplierEvent((Entity) (Object) this, returnValue);
        KamiMod.EVENT_BUS.post(event);
        if (!event.isCancelled() && event.getMultiplier() != returnValue) {
            cir.setReturnValue(event.getMultiplier());
        }
    }

    @Inject(method = "changeLookDirection", at = @At("HEAD"), cancellable = true)
    public void onChangeLookDirection(double cursorDeltaX, double cursorDeltaY, CallbackInfo ci) {
        UpdateLookEvent event = new UpdateLookEvent(cursorDeltaX, cursorDeltaY);
        KamiMod.EVENT_BUS.post(event);
        if (event.isCancelled()) ci.cancel();
    }

}
