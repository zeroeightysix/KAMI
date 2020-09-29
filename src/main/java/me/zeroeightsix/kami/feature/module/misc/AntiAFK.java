package me.zeroeightsix.kami.feature.module.misc;

import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.TickEvent;
import me.zeroeightsix.kami.feature.module.Module;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;

import java.util.Objects;
import java.util.Random;

@Module.Info(name = "AntiAFK", category = Module.Category.MISC, description = "Moves in order not to get kicked. (May be invisible client-sided)")
public class AntiAFK extends Module {

    @Setting(name = "Swing")
    private boolean swing = true;
    @Setting(name = "Turn")
    private boolean turn = true;

    private Random random = new Random();

    @EventHandler
    private Listener<TickEvent.InGame> updateListener = new Listener<>(event -> {
        assert mc.interactionManager != null;
        if (mc.interactionManager.isBreakingBlock()) return;

        ClientPlayerEntity player = event.getPlayer();

        if (player.age % 40 == 0 && swing)
            Objects.requireNonNull(mc.getNetworkHandler()).getConnection().send(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND));
        if (player.age % 15 == 0 && turn)
            player.yaw = random.nextFloat() * 360f - 180f;

        if (!(swing || turn) && player.age % 80 == 0) {
            player.jump();
        }
    });

}
