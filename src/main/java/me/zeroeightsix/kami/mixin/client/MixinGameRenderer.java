package me.zeroeightsix.kami.mixin.client;

import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.event.events.CameraHurtEvent;
import me.zeroeightsix.kami.event.events.RenderEvent;
import me.zeroeightsix.kami.event.events.RenderHudEvent;
import me.zeroeightsix.kami.feature.module.misc.NoEntityTrace;
import me.zeroeightsix.kami.util.Wrapper;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.function.Predicate;

@Mixin(GameRenderer.class)
public class MixinGameRenderer {

    @Inject(method = "renderWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", ordinal = 15), cancellable = true)
    public void renderWorld(float tickDelta, long limitTime, MatrixStack matrixStack, CallbackInfo ci) {
        RenderEvent.World worldRenderEvent = new RenderEvent.World(Tessellator.getInstance(), Wrapper.getRenderPosition(), matrixStack);
        KamiMod.EVENT_BUS.post(worldRenderEvent);
        if (worldRenderEvent.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "bobViewWhenHurt", at = @At("HEAD"), cancellable = true)
    private void bobViewWhenHurt(MatrixStack matrixStack, float f, CallbackInfo ci) {
        CameraHurtEvent event = new CameraHurtEvent(f);
        KamiMod.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Redirect(method = "updateTargetedEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/projectile/ProjectileUtil;rayTrace(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Box;Ljava/util/function/Predicate;D)Lnet/minecraft/util/hit/EntityHitResult;"))
    private EntityHitResult rayTrace(Entity entity, Vec3d vec3d, Vec3d vec3d2, Box box, Predicate<Entity> predicate, double d) {
        if (NoEntityTrace.shouldBlock()) {
            return null;
        } else {
            return ProjectileUtil.rayTrace(entity, vec3d, vec3d2, box, predicate, d);
        }
    }

    @Inject(
            method = "render",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;render(Lnet/minecraft/client/util/math/MatrixStack;F)V"),
            locals = LocalCapture.CAPTURE_FAILHARD,
            cancellable = true)
    public void onRender(float tickDelta,
                         long startTime,
                         boolean tick,
                         CallbackInfo ci,
                         int i,
                         int j,
                         Window window,
                         MatrixStack stack) {
        RenderHudEvent event = new RenderHudEvent(window, stack);
        KamiMod.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

}
