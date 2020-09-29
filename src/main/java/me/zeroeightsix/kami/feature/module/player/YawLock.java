package me.zeroeightsix.kami.feature.module.player;

import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.TickEvent;
import me.zeroeightsix.kami.feature.module.Module;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

@Module.Info(name = "YawLock", category = Module.Category.PLAYER)
public class YawLock extends Module {

    @Setting(name = "Auto")
    private boolean auto = true;
    @Setting(name = "Yaw")
    private float yaw = 180f;
    @Setting(name = "Slice")
    private int slice = 8;

    @EventHandler
    private Listener<TickEvent.InGame> updateListener = new Listener<>(event -> {
        ClientPlayerEntity player = event.getPlayer();
        
        if (slice == 0) return;
        if (auto) {
            int angle = 360 / slice;
            float yaw = player.yaw;
            yaw = Math.round(yaw / angle) * angle;
            player.yaw = yaw;
            Entity vehicle = player.getVehicle();
            if (player.isRiding() && vehicle != null) vehicle.yaw = yaw;
        } else {
            player.yaw = MathHelper.clamp(yaw - 180, -180, 180);
        }
    });
}
