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
@Module.Info(name = "YawLock", category = Module.Category.PLAYER)
public class YawLock extends Module {
    private Setting<Boolean> auto = register(Settings.b("Auto", true));
    private Setting<Float> yaw = register(Settings.f("Yaw", 180));
    private Setting<Integer> slice = register(Settings.i("Slice", 8));

    @EventHandler
    private Listener<TickEvent.Client> updateListener = new Listener<>(event -> {
        if (slice.getValue() == 0) return;
        if (auto.getValue()) {
            int angle = 360 / slice.getValue();
            float yaw = mc.player.yaw;
            yaw = Math.round(yaw / angle) * angle;
            mc.player.yaw = yaw;
            if (mc.player.isRiding()) mc.player.getVehicle().yaw = yaw;
        } else {
            mc.player.yaw = MathHelper.clamp(yaw.getValue() - 180, -180, 180);
        }
    });
}
