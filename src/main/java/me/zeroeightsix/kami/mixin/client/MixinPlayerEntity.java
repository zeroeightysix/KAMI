package me.zeroeightsix.kami.mixin.client;

import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.event.events.ClipAtLedgeEvent;
import me.zeroeightsix.kami.event.events.EntityEvent;
import me.zeroeightsix.kami.event.events.PlayerAttackEntityEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class MixinPlayerEntity {

    @ModifyVariable(method = "applyDamage", at = @At("STORE"), name = "damage", ordinal = 2)
    public int modifyDamage(int damage) {
        EntityEvent.EntityDamage damageEvent = new EntityEvent.EntityDamage((PlayerEntity) (Object) this, damage);
        KamiMod.EVENT_BUS.post(damageEvent);
        if (damageEvent.isCancelled()) {
            damage = 0;
        } else {
            damage = damageEvent.getDamage();
        }
        return damage;
    }

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
