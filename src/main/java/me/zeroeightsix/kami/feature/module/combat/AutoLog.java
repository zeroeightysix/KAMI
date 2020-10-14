package me.zeroeightsix.kami.feature.module.combat;

import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.EntityEvent;
import me.zeroeightsix.kami.event.TickEvent;
import me.zeroeightsix.kami.feature.module.AutoReconnect;
import me.zeroeightsix.kami.feature.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.s2c.play.DisconnectS2CPacket;
import net.minecraft.text.LiteralText;

import java.util.Objects;

@Module.Info(name = "AutoLog", description = "Automatically log when in danger or on low health", category = Module.Category.COMBAT)
public class AutoLog extends Module {

    @Setting(name = "Health")
    private @Setting.Constrain.Range(min = 0, max = 36) int health = 6;
    private boolean shouldLog = false;
    long lastLog = System.currentTimeMillis();

    @EventHandler
    private Listener<EntityEvent.UpdateHealth> livingDamageEventListener = new Listener<>(event -> {
        if (mc.player == null) return;
        if (event.getHealth() > 0f && event.getEntity() == mc.player && event.getHealth() < health) {
            log();
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
    private Listener<TickEvent.InGame> updateListener = new Listener<>(event -> {
        if (shouldLog) {
            shouldLog = false;
            if (System.currentTimeMillis() - lastLog < 2000) return;
            Objects.requireNonNull(MinecraftClient.getInstance().getNetworkHandler()).onDisconnect(new DisconnectS2CPacket(new LiteralText("AutoLogged")));
        }
    });

    private void log() {
        AutoReconnect.INSTANCE.disable();
        shouldLog = true;
        lastLog = System.currentTimeMillis();
    }

}
