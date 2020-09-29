package me.zeroeightsix.kami.feature.module.player;

import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.TickEvent;
import me.zeroeightsix.kami.feature.module.Module;

@Module.Info(name = "AutoWalk", category = Module.Category.PLAYER)
public class AutoWalk extends Module {

    @Setting(name = "Mode")
    private AutoWalkMode mode = AutoWalkMode.FORWARD;

    private Listener<TickEvent.InGame> tickListener = new Listener<>(event -> {
        assert mc.player != null;
        mc.player.input.movementForward = mode.forward;
    });

    private enum AutoWalkMode {
        FORWARD(1), BACKWARDS(-1);
        final int forward;

        AutoWalkMode(int forward) {
            this.forward = forward;
        }
    }

}
