package me.zeroeightsix.kami.feature.module.movement;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.events.EntityBlockCollisionEvent;
import me.zeroeightsix.kami.event.events.InputUpdateEvent;
import me.zeroeightsix.kami.feature.module.Module;
import net.minecraft.block.SoulSandBlock;

/**
 * Created by 086 on 15/12/2017.
 */
@Module.Info(name = "NoSlowDown", category = Module.Category.MOVEMENT)
public class NoSlowDown extends Module {

    @EventHandler
    private Listener<InputUpdateEvent> eventListener = new Listener<>(event -> {
        //
        // InputUpdateEvent is called just before the player is slowed down @see EntityPlayerSP.onLivingUpdate)
        // We'll abuse this fact, and multiply moveStrafe and moveForward by 5 to nullify the *0.2f hardcoded by mojang.
        //

        // Check if the player should be slowed down or not
        if (mc.player.isUsingItem() && !mc.player.isRiding()) {
            event.getNewState().movementSideways *= 5;
            event.getNewState().movementForward *= 5;
        }
    });

    @EventHandler
    private Listener<EntityBlockCollisionEvent> blockCollisionEventListener = new Listener<>(event -> {
       if (event.getState().getBlock() instanceof SoulSandBlock) {
           event.cancel();
       }
    });

    // Check MixinBlockSoulSand for soulsand slowdown nullification

}
