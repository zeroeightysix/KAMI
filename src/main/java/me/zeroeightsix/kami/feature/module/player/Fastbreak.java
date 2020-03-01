package me.zeroeightsix.kami.feature.module.player;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.events.TickEvent;
import me.zeroeightsix.kami.mixin.client.IMinecraftClient;
import me.zeroeightsix.kami.feature.module.Module;

/**
 * @author 086
 */
@Module.Info(name = "Fastbreak", category = Module.Category.PLAYER, description = "Nullifies block hit delay")
public class Fastbreak extends Module {

    @EventHandler
    private Listener<TickEvent.Client> updateListener = new Listener<>(event -> {
        ((IMinecraftClient) mc).setItemUseCooldown(0);
    });

}
