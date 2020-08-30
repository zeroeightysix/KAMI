package me.zeroeightsix.kami.mixin.client;

import me.zeroeightsix.kami.Colour;
import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.event.RenderWeatherEvent;
import me.zeroeightsix.kami.feature.module.ESP;
import me.zeroeightsix.kami.feature.module.Freecam;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.ShaderEffect;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.math.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public abstract class MixinWorldRenderer {

    @Shadow
    @Final
    private MinecraftClient client;

    @Shadow
    private Framebuffer entityOutlinesFramebuffer;

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/OutlineVertexConsumerProvider;draw()V"))
    public void onDraw(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo ci) {
        // Render the vertices in the buffer
        Framebuffer entityOutlinesFramebuffer = this.entityOutlinesFramebuffer;
        this.entityOutlinesFramebuffer = ESP.INSTANCE.getEntityOutlinesFramebuffer();

        ESP.INSTANCE.getOutlineConsumerProvider().draw();

        this.entityOutlinesFramebuffer = entityOutlinesFramebuffer;

        // Apply the outline shader to our freshly drawn-to framebuffer
        ESP.INSTANCE.getOutlineShader().render(tickDelta);
        this.client.getFramebuffer().beginWrite(false);
    }

    @Inject(method = "renderWeather", at = @At("HEAD"), cancellable = true)
    public void onRenderWeather(LightmapTextureManager manager, float f, double d, double e, double g, CallbackInfo ci) {
        RenderWeatherEvent event = new RenderWeatherEvent(manager, f, d, e, g);
        KamiMod.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;isThirdPerson()Z"))
    public boolean isThirdPerson(Camera camera) {
        if (Freecam.INSTANCE.getEnabled()) return true;
        return camera.isThirdPerson();
    }

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;setupTerrain(Lnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/Frustum;ZIZ)V"), index = 4)
    public boolean isSpectator(boolean isSpectator) {
        return Freecam.INSTANCE.getEnabled() || isSpectator;
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;renderEntity(Lnet/minecraft/entity/Entity;DDDFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;)V"))
    public void renderEntity(WorldRenderer worldRenderer, Entity entity, double cameraX, double cameraY, double cameraZ, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers) {
        if (ESP.INSTANCE.getEnabled()) {
            Colour colour = ESP.INSTANCE.getTargets().belongs(entity);
            if (colour != null) {
                Framebuffer entityOutlinesFramebuffer = this.entityOutlinesFramebuffer;
                this.entityOutlinesFramebuffer = ESP.INSTANCE.getEntityOutlinesFramebuffer();

                OutlineVertexConsumerProvider provider = ESP.INSTANCE.getOutlineConsumerProvider();
                provider.setColor((int) (colour.getR() * 255), (int) (colour.getG() * 255), (int) (colour.getB() * 255), (int) (colour.getA() * 255));
                this.renderEntity(entity, cameraX, cameraY, cameraZ, tickDelta, matrices, provider);

                this.entityOutlinesFramebuffer = entityOutlinesFramebuffer;
                return;
            }
        }
        this.renderEntity(entity, cameraX, cameraY, cameraZ, tickDelta, matrices, vertexConsumers);
    }

    @Shadow
    protected abstract void renderEntity(Entity entity, double cameraX, double cameraY, double cameraZ, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers);

    @Inject(method = "close", at = @At("RETURN"))
    public void onClose(CallbackInfo ci) {
        ESP.INSTANCE.closeShader();
    }

    @Inject(method = "apply", at = @At("RETURN"))
    public void onApply(ResourceManager manager, CallbackInfo ci) {
        // (Re)load sharp entity outline shader
        ESP.INSTANCE.loadOutlineShader();
    }

    @Inject(method = "onResized", at = @At("RETURN"))
    public void onResized(int i, int j, CallbackInfo ci) {
        ShaderEffect shader = ESP.INSTANCE.getOutlineShader();
        if (shader != null) shader.setupDimensions(i, j);
    }

    @Inject(method = "render", at = @At("HEAD"))
    public void onRender(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo ci) {
        ESP.INSTANCE.getEntityOutlinesFramebuffer().clear(MinecraftClient.IS_SYSTEM_MAC);
        this.client.getFramebuffer().beginWrite(false);
    }

    @Inject(method = "drawEntityOutlinesFramebuffer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gl/Framebuffer;draw(IIZ)V"))
    public void onDrawEntityOutlinesFramebuffer(CallbackInfo ci) {
        ESP.INSTANCE.getEntityOutlinesFramebuffer().draw(this.client.getWindow().getFramebufferWidth(), this.client.getWindow().getFramebufferHeight(), false);
    }

}
