package me.zeroeightsix.kami.feature.module.misc;

import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.TickEvent;
import me.zeroeightsix.kami.feature.module.Module;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;

import java.util.Random;

/**
 * Created by 086 on 16/12/2017.
 */
@Module.Info(name = "AntiAFK", category = Module.Category.MISC, description = "Moves in order not to get kicked. (May be invisible client-sided)")
public class AntiAFK extends Module {

    @Setting(name = "Swing")
    private boolean swing = true;
    @Setting(name = "Turn")
    private boolean turn = true;

    private Random random = new Random();

    @EventHandler
    private Listener<TickEvent.Client.InGame> updateListener = new Listener<>(event -> {
        if (mc.interactionManager.isBreakingBlock()) return;

        if (mc.player.age % 40 == 0 && swing)
            mc.getNetworkHandler().getConnection().send(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND));
        if (mc.player.age % 15 == 0 && turn)
            mc.player.yaw = random.nextInt(360) - 180;

        if (!(swing || turn) && mc.player.age % 80 == 0) {
            mc.player.jump();
        }
    });

}
