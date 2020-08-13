package me.zeroeightsix.kami.feature.module.player;

import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.TickEvent;
import me.zeroeightsix.kami.feature.module.Module;
import net.minecraft.util.math.MathHelper;

/**
 * Created by 086 on 16/12/2017.
 */
@Module.Info(name = "YawLock", category = Module.Category.PLAYER)
public class YawLock extends Module {

    @Setting(name = "Auto")
    private boolean auto = true;
    @Setting(name = "Yaw")
    private float yaw = 180f;
    @Setting(name = "Slice")
    private int slice = 8;

    @EventHandler
    private Listener<TickEvent.Client.InGame> updateListener = new Listener<>(event -> {
        if (slice == 0) return;
        if (auto) {
            int angle = 360 / slice;
            float yaw = mc.player.yaw;
            yaw = Math.round(yaw / angle) * angle;
            mc.player.yaw = yaw;
            if (mc.player.isRiding()) mc.player.getVehicle().yaw = yaw;
        } else {
            mc.player.yaw = MathHelper.clamp(yaw - 180, -180, 180);
        }
    });
}
