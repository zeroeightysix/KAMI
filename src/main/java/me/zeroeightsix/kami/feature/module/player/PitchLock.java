package me.zeroeightsix.kami.feature.module.player;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.events.TickEvent;
import me.zeroeightsix.kami.feature.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraft.util.math.MathHelper;

/**
 * Created by 086 on 16/12/2017.
 */
@Module.Info(name = "PitchLock", category = Module.Category.PLAYER)
public class PitchLock extends Module {
    private Setting<Boolean> auto = register(Settings.b("Auto", true));
    private Setting<Float> pitch = register(Settings.f("Pitch", 180));
    private Setting<Integer> slice = register(Settings.i("Slice", 8));

    @EventHandler
    private Listener<TickEvent.Client> updateListener = new Listener<>(event -> {
        if (slice.getValue() == 0) return;
        if (auto.getValue()) {
            int angle = 360 / slice.getValue();
            float yaw = mc.player.pitch;
            yaw = Math.round(yaw / angle) * angle;
            mc.player.pitch = yaw;
            if (mc.player.isRiding()) mc.player.getVehicle().pitch = yaw;
        } else {
            mc.player.pitch = MathHelper.clamp(pitch.getValue() - 180, -180, 180);
        }
    });

}
