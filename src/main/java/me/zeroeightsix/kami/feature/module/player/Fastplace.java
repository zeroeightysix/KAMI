package me.zeroeightsix.kami.feature.module.player;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.TickEvent;
import me.zeroeightsix.kami.feature.module.Module;
import me.zeroeightsix.kami.mixin.client.IMinecraftClient;

@Module.Info(name = "Fastplace", category = Module.Category.PLAYER, description = "Nullifies block place delay")
public class Fastplace extends Module {

    @EventHandler
    private Listener<TickEvent.InGame> updateListener = new Listener<>(event -> {
        ((IMinecraftClient) mc).setItemUseCooldown(0);
    });

}
