package me.zeroeightsix.kami.feature.module.player;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.TickEvent;
import me.zeroeightsix.kami.feature.module.Module;
import me.zeroeightsix.kami.util.EntityUtil;
import net.minecraft.client.network.ClientPlayerEntity;

@Module.Info(name = "AutoJump", category = Module.Category.PLAYER, description = "Automatically jumps if possible")
public class AutoJump extends Module {

    @EventHandler
    private Listener<TickEvent.InGame> updateListener = new Listener<>(event -> {
        ClientPlayerEntity player = event.getPlayer();

        if (player.isSubmergedInWater() || player.isInLava()) {
            EntityUtil.updateVelocityY(player, 0.1);
        } else if (player.isOnGround()) player.jump();
    });

}
