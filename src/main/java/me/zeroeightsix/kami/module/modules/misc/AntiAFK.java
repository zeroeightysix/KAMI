package me.zeroeightsix.kami.module.modules.misc;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.events.TickEvent;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraft.server.network.packet.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;

import java.util.Random;

/**
 * Created by 086 on 16/12/2017.
 */
@Module.Info(name = "AntiAFK", category = Module.Category.MISC, description = "Moves in order not to get kicked. (May be invisible client-sided)")
public class AntiAFK extends Module {

    private Setting<Boolean> swing = register(Settings.b("Swing", true));
    private Setting<Boolean> turn = register(Settings.b("Turn", true));

    private Random random = new Random();

    @EventHandler
    private Listener<TickEvent.Client> updateListener = new Listener<>(event -> {
        if (mc.interactionManager.isBreakingBlock()) return;

        if (mc.player.age % 40 == 0 && swing.getValue())
            mc.getNetworkHandler().getConnection().send(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND));
        if (mc.player.age % 15 == 0 && turn.getValue())
            mc.player.yaw = random.nextInt(360) - 180;

        if (!(swing.getValue() || turn.getValue()) && mc.player.age % 80 == 0) {
            mc.player.jump();
        }
    });

}
