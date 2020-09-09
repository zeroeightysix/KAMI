package me.zeroeightsix.kami.mixin.client;

import com.mojang.blaze3d.systems.RenderSystem;
import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.event.CameraHurtEvent;
import me.zeroeightsix.kami.event.RenderEvent;
import me.zeroeightsix.kami.event.TargetEntityEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.function.Predicate;

@Mixin(GameRenderer.class)
public class MixinGameRenderer {

    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(method = "renderWorld",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/render/WorldRenderer;render(Lnet/minecraft/client/util/math/MatrixStack;FJZLnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/GameRenderer;Lnet/minecraft/client/render/LightmapTextureManager;Lnet/minecraft/util/math/Matrix4f;)V",
                    shift = At.Shift.AFTER),
            cancellable = true,
            locals = LocalCapture.CAPTURE_FAILHARD)
    public void renderWorld(float tickDelta, long limitTime, MatrixStack matrixStack1, CallbackInfo ci, boolean bl, Camera camera, MatrixStack matrixStack2, Matrix4f matrix4f) {
        client.getProfiler().push("kamiWorldRender");

        RenderEvent.World worldRenderEvent = new RenderEvent.World(tickDelta, matrixStack1, matrix4f);
        RenderSystem.pushMatrix();
        RenderSystem.multMatrix(matrixStack1.peek().getModel());
        KamiMod.EVENT_BUS.post(worldRenderEvent);
        RenderSystem.popMatrix();
        if (worldRenderEvent.isCancelled()) {
            ci.cancel();
        }

        client.getProfiler().pop();
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
        EntityHitResult result = ProjectileUtil.rayTrace(entity, vec3d, vec3d2, box, predicate, d);
        TargetEntityEvent event = new TargetEntityEvent(entity, vec3d, vec3d2, box, predicate, d, result);
        KamiMod.EVENT_BUS.post(event);
        return event.getTrace();
    }

}
