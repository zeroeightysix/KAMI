package me.zeroeightsix.kami.mixin.client;

import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.event.ClipAtLedgeEvent;
import me.zeroeightsix.kami.event.EntityEvent;
import me.zeroeightsix.kami.event.PlayerAttackEntityEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class MixinPlayerEntity {

    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    public void attack(Entity entity, CallbackInfo info) {
        PlayerAttackEntityEvent event = new PlayerAttackEntityEvent(entity);
        KamiMod.EVENT_BUS.post(event);
        if (info.isCancelled()) {
            info.cancel();
        }
    }

    @Inject(method = "clipAtLedge", at = @At("HEAD"), cancellable = true)
    public void onClipAtLedge(CallbackInfoReturnable<Boolean> cir) {
        ClipAtLedgeEvent event = new ClipAtLedgeEvent((PlayerEntity) (Object) this, null);
        KamiMod.EVENT_BUS.post(event);
        if (event.getClip() != null) cir.setReturnValue(event.getClip());
    }

}
