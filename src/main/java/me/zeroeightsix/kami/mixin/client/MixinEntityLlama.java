package me.zeroeightsix.kami.mixin.client;

import me.zeroeightsix.kami.module.ModuleManager;
import net.minecraft.entity.passive.LlamaEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Created by 086 on 15/10/2018.
 */
@Mixin(LlamaEntity.class)
public class MixinEntityLlama {

    @Inject(method = "canBeControlledByRider", at = @At("RETURN"), cancellable = true)
    public void onCanBeControlledByRider(CallbackInfoReturnable<Boolean> returnable) {
        if (ModuleManager.isModuleEnabled("EntitySpeed")) returnable.setReturnValue(true);
    }

}
