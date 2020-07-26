package me.zeroeightsix.kami.mixin.client;

import io.github.fablabsmc.fablabs.api.fiber.v1.exception.FiberException;
import io.github.fablabsmc.fablabs.api.fiber.v1.exception.FiberTypeProcessingException;
import io.github.fablabsmc.fablabs.api.fiber.v1.exception.ProcessingMemberException;
import io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.derived.ConfigType;
import me.zeroeightsix.kami.KotlinTypeMagic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

@Mixin(targets = "io.github.fablabsmc.fablabs.impl.fiber.annotation.AnnotatedSettingsImpl$PojoMemberProcessorImpl")
public abstract class MixinPojoMemberProcessorImpl {

    @Shadow
    protected abstract <R, S> void processSetting(Object pojo, Field setting, ConfigType<R, S, ?> type) throws FiberException;

    @Shadow
    protected abstract ConfigType<?, ?, ?> toConfigType(AnnotatedType annotatedType) throws FiberTypeProcessingException;

    @Inject(method = "processSetting(Ljava/lang/Object;Ljava/lang/reflect/Field;)V", at = @At(value = "INVOKE", target = "Lio/github/fablabsmc/fablabs/impl/fiber/annotation/AnnotatedSettingsImpl$PojoMemberProcessorImpl;processSetting(Ljava/lang/Object;Ljava/lang/reflect/Field;Lio/github/fablabsmc/fablabs/api/fiber/v1/schema/type/derived/ConfigType;)V"), cancellable = true, remap = false)
    public void invokeProcessSetting(Object pojo, Field setting, CallbackInfo ci) throws ProcessingMemberException {
        ci.cancel();
        AnnotatedType type = KotlinTypeMagic.getKotlinAnnotatedType(pojo, setting);
        try {
            processSetting(pojo, setting, toConfigType(type));
        } catch (FiberException e) {
            throw new ProcessingMemberException("Failed to process setting '" + Modifier.toString(setting.getModifiers()) + " " + setting.getType().getSimpleName() + " " + setting.getName() + "' in " + setting.getDeclaringClass().getSimpleName(), e, setting);
        }
    }

}
