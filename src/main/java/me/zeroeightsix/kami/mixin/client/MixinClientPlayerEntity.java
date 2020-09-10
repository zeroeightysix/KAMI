package me.zeroeightsix.kami.mixin.client;

import com.mojang.authlib.GameProfile;
import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.event.CloseScreenInPortalEvent;
import me.zeroeightsix.kami.event.PlayerMoveEvent;
import me.zeroeightsix.kami.feature.module.NoSlowDown;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.MovementType;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public abstract class MixinClientPlayerEntity extends AbstractClientPlayerEntity {

    @Shadow
    public Input input;

    public MixinClientPlayerEntity(ClientWorld world, GameProfile profile) {
        super(world, profile);
    }

    @Inject(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/input/Input;tick(Z)V", shift = At.Shift.AFTER))
    public void onTickMovement(CallbackInfo ci) {
        // Cancel item use slowdown
        if (NoSlowDown.INSTANCE.getEnabled() && this.isUsingItem() && !this.hasVehicle()) {
            Input var10000 = this.input;
            var10000.movementForward /= 0.2F;
            var10000.movementSideways /= 0.2F;
        }
    }

    @Redirect(method = "updateNausea", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;openScreen(Lnet/minecraft/client/gui/screen/Screen;)V"))
    public void openScreen(MinecraftClient client, Screen screen) {
        CloseScreenInPortalEvent event = new CloseScreenInPortalEvent(screen);
        KamiMod.EVENT_BUS.post(event);
        if (!event.isCancelled()) {
            client.openScreen(screen);
        }
    }

    @Shadow
    public abstract boolean isUsingItem();

    @Inject(method = "move", cancellable = true, at = @At("HEAD"))
    public void move(MovementType type, Vec3d vec, CallbackInfo info) {
        PlayerMoveEvent event = new PlayerMoveEvent(type, vec);
        KamiMod.EVENT_BUS.post(event);
        if (event.isCancelled()) info.cancel();
    }

}
