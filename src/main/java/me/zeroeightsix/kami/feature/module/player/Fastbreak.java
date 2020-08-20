package me.zeroeightsix.kami.feature.module.player;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.TickEvent;
import me.zeroeightsix.kami.feature.module.Module;
import me.zeroeightsix.kami.mixin.client.IClientPlayerInteractionManager;

@Module.Info(name = "Fastbreak", category = Module.Category.PLAYER, description = "Nullifies block hit delay")
public class Fastbreak extends Module {

    @EventHandler
    private Listener<TickEvent.Client.InGame> updateListener = new Listener<>(event -> {
        ((IClientPlayerInteractionManager) mc.interactionManager).setBlockBreakingCooldown(0);
    });

}
