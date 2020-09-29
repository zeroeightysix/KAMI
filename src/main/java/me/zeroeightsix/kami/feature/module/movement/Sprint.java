package me.zeroeightsix.kami.feature.module.movement;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.TickEvent;
import me.zeroeightsix.kami.feature.module.Module;
import net.minecraft.client.network.ClientPlayerEntity;

@Module.Info(name = "Sprint", description = "Automatically makes the player sprint", category = Module.Category.MOVEMENT)
public class Sprint extends Module {

    @EventHandler
    private Listener<TickEvent.InGame> updateListener = new Listener<>(event -> {
        ClientPlayerEntity player = event.getPlayer();
        player.setSprinting(!player.horizontalCollision && player.forwardSpeed > 0);
    });

}
