package me.zeroeightsix.kami.mixin.client;

import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.event.ScreenEvent;
import me.zeroeightsix.kami.event.TickEvent;
import me.zeroeightsix.kami.setting.KamiConfig;
import me.zeroeightsix.kami.util.Wrapper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;

/**
 * Created by 086 on 17/11/2017.
 */
@Mixin(MinecraftClient.class)
public class MixinMinecraftClient {

    @Shadow public ClientWorld world;

    @Shadow public ClientPlayerEntity player;

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiler/Profiler;pop()V", ordinal = 0), cancellable = true)
    public void tick(CallbackInfo info) {
        TickEvent.Client event;
        if (player != null && world != null) {
            event = new TickEvent.Client.InGame();
        } else {
            event = new TickEvent.Client.OutOfGame();
        }
        KamiMod.EVENT_BUS.post(event);
        if (event.isCancelled()) info.cancel();
    }

    @ModifyVariable(method = "openScreen", at = @At("HEAD"))
    private Screen openScreen(Screen screen) {
        ScreenEvent.Closed closedEvent = new ScreenEvent.Closed(Wrapper.getMinecraft().currentScreen);
        KamiMod.EVENT_BUS.post(closedEvent);
        if (closedEvent.isCancelled()) {
            return Wrapper.getMinecraft().currentScreen;
        }
        ScreenEvent.Displayed displayedEvent = new ScreenEvent.Displayed(screen);
        KamiMod.EVENT_BUS.post(displayedEvent);
        if (displayedEvent.isCancelled()) {
            return Wrapper.getMinecraft().currentScreen;
        }
        return displayedEvent.getScreen();
    }

    @Inject(method = "run", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;printCrashReport(Lnet/minecraft/util/crash/CrashReport;)V"))
    public void displayCrashReport(CallbackInfo info) {
        save();
    }

    @Inject(method = "stop", at = @At("HEAD"))
    public void shutdown(CallbackInfo info) {
        save();
    }

    private void save() {
        System.out.println("Shutting down: saving KAMI configuration");
        try {
            KamiConfig.saveConfiguration();
            System.out.println("Configuration saved.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
