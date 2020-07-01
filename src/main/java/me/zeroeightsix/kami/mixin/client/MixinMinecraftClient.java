package me.zeroeightsix.kami.mixin.client;

import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.event.events.ScreenEvent;
import me.zeroeightsix.kami.event.events.TickEvent;
import me.zeroeightsix.kami.setting.KamiConfig;
import me.zeroeightsix.kami.util.Wrapper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;

/**
 * Created by 086 on 17/11/2017.
 */
@Mixin(MinecraftClient.class)
public class MixinMinecraftClient {

    @Shadow public ClientWorld world;

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiler/DisableableProfiler;push(Ljava/lang/String;)V", ordinal = 0), cancellable = true)
    public void tick(CallbackInfo info) {
        TickEvent.Client event;
        if (Wrapper.getMinecraft().player != null && Wrapper.getMinecraft().world != null) {
            event = new TickEvent.Client.InGame();
        } else {
            event = new TickEvent.Client.OutOfGame();
        }
        KamiMod.EVENT_BUS.post(event);
        if (event.isCancelled()) info.cancel();
    }

    @Inject(method = "openScreen", at = @At("HEAD"), cancellable = true)
    public void openScreen(Screen guiScreenIn, CallbackInfo info) {
        ScreenEvent.Closed closedEvent = new ScreenEvent.Closed(Wrapper.getMinecraft().currentScreen);
        KamiMod.EVENT_BUS.post(closedEvent);
        if (closedEvent.isCancelled()) {
            info.cancel();
            return;
        }
        ScreenEvent.Displayed displayedEvent = new ScreenEvent.Displayed(guiScreenIn);
        KamiMod.EVENT_BUS.post(displayedEvent);
        if (displayedEvent.isCancelled()) {
            info.cancel();
        }
        guiScreenIn = displayedEvent.getScreen();
    }

    @Inject(method = "start", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;printCrashReport(Lnet/minecraft/util/crash/CrashReport;)V"))
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
            KamiConfig.saveConfiguration(KamiMod.getInstance().getConfig());
            System.out.println("Configuration saved.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
