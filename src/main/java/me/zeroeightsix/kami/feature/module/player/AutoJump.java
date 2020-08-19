package me.zeroeightsix.kami.feature.module.player;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.TickEvent;
import me.zeroeightsix.kami.feature.module.Module;
import me.zeroeightsix.kami.util.EntityUtil;

/**
 * Created by 086 on 24/12/2017.
 */
@Module.Info(name = "AutoJump", category = Module.Category.PLAYER, description = "Automatically jumps if possible")
public class AutoJump extends Module {

    @EventHandler
    private Listener<TickEvent.Client.InGame> updateListener = new Listener<>(event -> {
        if (mc.player.isSubmergedInWater() || mc.player.isInLava()) {
            EntityUtil.updateVelocityY(mc.player, 0.1);
        }
        else if (mc.player.isOnGround()) mc.player.jump();
    });

}
