package me.zeroeightsix.kami.mixin.client;

import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.event.events.CloseScreenInPortalEvent;
import me.zeroeightsix.kami.event.events.InputUpdateEvent;
import me.zeroeightsix.kami.event.events.PlayerMoveEvent;
import me.zeroeightsix.kami.mixin.extend.ExtendedInput;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public class MixinClientPlayerEntity {

    @Redirect(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/input/Input;tick(ZZ)V"))
    public void tick(Input input, boolean bl, boolean bl2) {
        Input prev = ((ExtendedInput) input).copy(); // Create a copy of the previous input state
        input.tick(bl, bl2); // Update the current one
        InputUpdateEvent ev = new InputUpdateEvent(prev, input); // fire an event to notify the (potential) change in input
        KamiMod.EVENT_BUS.post(ev);
        if (ev.isCancelled()) ((ExtendedInput) input).update(prev); // revert to old when event is cancelled
        // we don't need to mutate input again as any listener that did mutate it, mutated the one minecraft uses
    }

    @Redirect(method = "updateNausea", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;openScreen(Lnet/minecraft/client/gui/screen/Screen;)V"))
    public void openScreen(MinecraftClient client, Screen screen) {
        CloseScreenInPortalEvent event = new CloseScreenInPortalEvent(screen);
        KamiMod.EVENT_BUS.post(event);
        if (!event.isCancelled()) {
            client.openScreen(screen);
        }
    }

    @Inject(method = "move", cancellable = true, at = @At("HEAD"))
    public void move(MovementType type, Vec3d vec, CallbackInfo info) {
        PlayerMoveEvent event = new PlayerMoveEvent(type, vec);
        KamiMod.EVENT_BUS.post(event);
        if (event.isCancelled()) info.cancel();
    }

}