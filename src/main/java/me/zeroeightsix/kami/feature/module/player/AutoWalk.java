package me.zeroeightsix.kami.feature.module.player;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.fiber.api.annotation.Setting;
import me.zeroeightsix.fiber.api.annotation.Settings;
import me.zeroeightsix.kami.event.events.InputUpdateEvent;
import me.zeroeightsix.kami.feature.module.Module;

/**
 * Created by 086 on 16/12/2017.
 */
@Module.Info(name = "AutoWalk", category = Module.Category.PLAYER)
@Settings(onlyAnnotated = true)
public class AutoWalk extends Module {

    @Setting(name = "Mode")
    private AutoWalkMode mode = AutoWalkMode.FORWARD;

    @EventHandler
    private Listener<InputUpdateEvent> inputUpdateEventListener = new Listener<>(event -> {
        switch (mode) {
            case FORWARD:
                event.getNewState().movementForward = 1;
                break;
            case BACKWARDS:
                event.getNewState().movementForward = -1;
                break;
        }
    });

    private enum AutoWalkMode {
        FORWARD, BACKWARDS
    }

}
