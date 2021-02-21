package me.zeroeightsix.kami.mixin.client;

import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.event.ScreenEvent;
import me.zeroeightsix.kami.event.TickEvent;
import me.zeroeightsix.kami.gui.KamiImgui;
import me.zeroeightsix.kami.setting.KamiConfig;
import me.zeroeightsix.kami.util.Wrapper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.profiler.Profiler;
import org.apache.logging.log4j.LogManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;

@Mixin(MinecraftClient.class)
public class MixinMinecraftClient {

    @Shadow
    public ClientWorld world;

    @Shadow
    public ClientPlayerEntity player;

    @Shadow
    private Profiler profiler;

    @Inject(method = "<init>", at = @At(value = "RETURN"))
    public void init(CallbackInfo info) {
        KamiImgui.INSTANCE.init();
        LogManager.getLogger("KAMI").info("ImGui initialised.");
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiler/Profiler;pop()V", ordinal = 0, shift = At.Shift.AFTER), cancellable = true)
    public void tick(CallbackInfo info) {
        TickEvent event;
        if (player != null && world != null) {
            event = new TickEvent.InGame(player, world);
        } else {
            event = new TickEvent.OutOfGame();
        }
        profiler.push("kamiTick");
        KamiMod.EVENT_BUS.post(event);
        profiler.pop();
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
            KamiConfig.INSTANCE.saveAll();
            System.out.println("Configuration saved.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
