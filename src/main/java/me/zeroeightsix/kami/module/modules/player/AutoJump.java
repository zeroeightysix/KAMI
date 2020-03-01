package me.zeroeightsix.kami.module.modules.player;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.events.TickEvent;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.util.EntityUtil;

/**
 * Created by 086 on 24/12/2017.
 */
@Module.Info(name = "AutoJump", category = Module.Category.PLAYER, description = "Automatically jumps if possible")
public class AutoJump extends Module {

    @EventHandler
    private Listener<TickEvent.Client> updateListener = new Listener<>(event -> {
        if (mc.player.isInWater() || mc.player.isInLava()) {
            EntityUtil.updateVelocityY(mc.player, 0.1);
        }
        else if (mc.player.onGround) mc.player.jump();
    });

}
