package me.zeroeightsix.kami.feature.module.combat;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting;
import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Settings;
import me.zeroeightsix.kami.event.events.EntityEvent;
import me.zeroeightsix.kami.event.events.TickEvent;
import me.zeroeightsix.kami.feature.module.AutoReconnect;
import me.zeroeightsix.kami.feature.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.packet.DisconnectS2CPacket;
import net.minecraft.text.LiteralText;

/**
 * Created by 086 on 9/04/2018.
 */
@Module.Info(name = "AutoLog", description = "Automatically log when in danger or on low health", category = Module.Category.COMBAT)
@Settings(onlyAnnotated = true)
public class AutoLog extends Module {

    // TODO: Step
    @Setting(name = "Health")
    private @Setting.Constrain.Range(min = 0, max = 36) int health = 6;
    private boolean shouldLog = false;
    long lastLog = System.currentTimeMillis();

    @EventHandler
    private Listener<EntityEvent.EntityDamage> livingDamageEventListener = new Listener<>(event -> {
        if (mc.player == null) return;
        if (event.getEntity() == mc.player) {
            if (mc.player.getHealth() - event.getDamage() < health) {
                log();
            }
        }
    });

    /*@EventHandler
    private Listener<EntityJoinWorldEvent> entityJoinWorldEventListener = new Listener<>(event -> {
        if (mc.player == null) return;
        if (event.getEntity() instanceof EnderCrystalEntity) {
            if (mc.player.getHealth() - CrystalAura.calculateDamage((EnderCrystalEntity) event.getEntity(), mc.player) < health) {
                log();
            }
        }
    });*/ //TODO

    @EventHandler
    private Listener<TickEvent.Client.InGame> updateListener = new Listener<>(event -> {
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
