package me.zeroeightsix.kami.mixin.client;

import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.event.events.CanBeSteeredEvent;
import net.minecraft.entity.mob.MobEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MobEntity.class)
public abstract class MixinMobEntity {

    @Inject(method = "canBeControlledByRider", at = @At("RETURN"))
    public void canBeControlledByRider(CallbackInfoReturnable<Boolean> returnable) {
        CanBeSteeredEvent event = new CanBeSteeredEvent((MobEntity) (Object) this, canBeControlledByRider());
        KamiMod.EVENT_BUS.post(event);
        returnable.setReturnValue(!event.isCancelled() && event.canBeSteered());
    }

    @Shadow
    public abstract boolean canBeControlledByRider();

}