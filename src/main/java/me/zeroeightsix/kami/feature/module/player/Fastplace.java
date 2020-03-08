package me.zeroeightsix.kami.feature.module.player;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.events.TickEvent;
import me.zeroeightsix.kami.mixin.client.IMinecraftClient;
import me.zeroeightsix.kami.feature.module.Module;

/**
 * @author 086
 */
@Module.Info(name = "Fastplace", category = Module.Category.PLAYER, description = "Nullifies block place delay")
public class Fastplace extends Module {

    @EventHandler
    private Listener<TickEvent.Client.InGame> updateListener = new Listener<>(event -> {
        ((IMinecraftClient) mc).setItemUseCooldown(0);
    });

}
