package me.zeroeightsix.kami.mixin.client;

import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.event.events.CanBeControlledEvent;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.StriderEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StriderEntity.class)
public class MixinStriderEntity {

    @Inject(method = "canBeControlledByRider", at = @At("TAIL"), cancellable = true)
    public void onCanBeControlledByRider(CallbackInfoReturnable<Boolean> cir) {
        CanBeControlledEvent event = new CanBeControlledEvent((MobEntity) (Object) this, null);
        KamiMod.EVENT_BUS.post(event);
        Boolean b = event.getCanBeSteered();
        if (!event.isCancelled() && b != null) {
            cir.setReturnValue(b);
        }
    }

}
