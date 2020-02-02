package me.zeroeightsix.kami.module.modules.player;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.events.InputUpdateEvent;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;

/**
 * Created by 086 on 16/12/2017.
 */
@Module.Info(name = "AutoWalk", category = Module.Category.PLAYER)
public class AutoWalk extends Module {

    private Setting<AutoWalkMode> mode = register(Settings.e("Mode", AutoWalkMode.FORWARD));

    @EventHandler
    private Listener<InputUpdateEvent> inputUpdateEventListener = new Listener<>(event -> {
        switch (mode.getValue()) {
            case FORWARD:
                event.getNewState().movementForward = 1;
                break;
            case BACKWARDS:
                event.getNewState().movementForward = -1;
                break;
        }
    });

    private static enum AutoWalkMode {
        FORWARD, BACKWARDS
    }

}
