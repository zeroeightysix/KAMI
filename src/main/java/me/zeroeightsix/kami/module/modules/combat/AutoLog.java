package me.zeroeightsix.kami.module.modules.combat;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.events.EntityEvent;
import me.zeroeightsix.kami.event.events.TickEvent;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.modules.AutoReconnect;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.packet.DisconnectS2CPacket;
import net.minecraft.text.LiteralText;

/**
 * Created by 086 on 9/04/2018.
 */
@Module.Info(name = "AutoLog", description = "Automatically log when in danger or on low health", category = Module.Category.COMBAT)
public class AutoLog extends Module {

    private Setting<Integer> health = register(Settings.integerBuilder("Health").withRange(0, 36).withValue(6).build());
    private boolean shouldLog = false;
    long lastLog = System.currentTimeMillis();

    @EventHandler
    private Listener<EntityEvent.EntityDamage> livingDamageEventListener = new Listener<>(event -> {
        if (mc.player == null) return;
        if (event.getEntity() == mc.player) {
            if (mc.player.getHealth() - event.getDamage() < health.getValue()) {
                log();
            }
        }
    });

    /*@EventHandler
    private Listener<EntityJoinWorldEvent> entityJoinWorldEventListener = new Listener<>(event -> {
        if (mc.player == null) return;
        if (event.getEntity() instanceof EnderCrystalEntity) {
            if (mc.player.getHealth() - CrystalAura.calculateDamage((EnderCrystalEntity) event.getEntity(), mc.player) < health.getValue()) {
                log();
            }
        }
    });*/ //TODO

    @EventHandler
    private Listener<TickEvent.Client> updateListener = new Listener<>(event -> {
        if (shouldLog) {
            shouldLog = false;
            if (System.currentTimeMillis() - lastLog < 2000) return;
            MinecraftClient.getInstance().getNetworkHandler().onDisconnect(new DisconnectS2CPacket(new LiteralText("AutoLogged")));
        }
    });

    private void log() {
        AutoReconnect.INSTANCE.disable();
        shouldLog = true;
        lastLog = System.currentTimeMillis();
    }

}
