package me.zeroeightsix.kami.feature.module.player;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.fiber.api.annotation.Setting;
import me.zeroeightsix.fiber.api.annotation.Settings;
import me.zeroeightsix.kami.event.events.TickEvent;
import me.zeroeightsix.kami.feature.module.Module;
import net.minecraft.util.math.MathHelper;

/**
 * Created by 086 on 16/12/2017.
 */
@Module.Info(name = "PitchLock", category = Module.Category.PLAYER)
@Settings(onlyAnnotated = true)
public class PitchLock extends Module {

    @Setting(name = "Auto")
    private boolean auto = true;
    @Setting(name = "Pitch")
    private float pitch = 180f;
    @Setting(name = "Slice")
    private int slice = 8;

    @EventHandler
    private Listener<TickEvent.Client.InGame> updateListener = new Listener<>(event -> {
        if (slice == 0) return;
        if (auto) {
            int angle = 360 / slice;
            float yaw = mc.player.pitch;
            yaw = Math.round(yaw / angle) * angle;
            mc.player.pitch = yaw;
            if (mc.player.isRiding()) mc.player.getVehicle().pitch = yaw;
        } else {
            mc.player.pitch = MathHelper.clamp(pitch - 180, -180, 180);
        }
    });

}
