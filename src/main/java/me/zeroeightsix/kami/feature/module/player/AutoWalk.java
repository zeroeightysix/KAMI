package me.zeroeightsix.kami.feature.module.player;

import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.InputUpdateEvent;
import me.zeroeightsix.kami.feature.module.Module;

@Module.Info(name = "AutoWalk", category = Module.Category.PLAYER)
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
