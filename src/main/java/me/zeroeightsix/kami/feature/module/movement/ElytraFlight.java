package me.zeroeightsix.kami.feature.module.movement;

import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.TickEvent;
import me.zeroeightsix.kami.feature.module.Module;
import me.zeroeightsix.kami.util.EntityUtil;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.util.math.MathHelper;

/**
 * Created by 086 on 11/04/2018.
 */
@Module.Info(name = "ElytraFlight", description = "Allows infinite elytra flying", category = Module.Category.MOVEMENT)
public class ElytraFlight extends Module {

    @Setting(name = "Mode")
    private ElytraFlightMode mode = ElytraFlightMode.BOOST;

    @EventHandler
    private Listener<TickEvent.Client.InGame> updateListener = new Listener<>(event -> {
        if (!mc.player.isFallFlying()) return;
        switch (mode) {
            case BOOST:
                if(mc.player.isSubmergedInWater())
                {
                    mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
                    return;
                }

                if(mc.options.keyJump.isPressed()) {
                    EntityUtil.updateVelocityY(mc.player, mc.player.getVelocity().y + 0.08);
                } else if(mc.options.keySneak.isPressed()) {
                    EntityUtil.updateVelocityY(mc.player, mc.player.getVelocity().y - 0.04);
                }

                if (mc.options.keyForward.isPressed()) {
                    float yaw = (float) Math.toRadians(mc.player.yaw);
                    mc.player.addVelocity(MathHelper.sin(yaw) * -0.05F, 0, MathHelper.cos(yaw) * 0.05F);
                } else if (mc.options.keyBack.isPressed()) {
                    float yaw = (float) Math.toRadians(mc.player.yaw);
                    mc.player.addVelocity(MathHelper.sin(yaw) * 0.05F, 0, MathHelper.cos(yaw) * -0.05F);
                }
                break;
            case FLY:
                mc.player.abilities.flying = true;
        }
    });

    @Override
    public void onDisable() {
        if (mc.player.abilities.creativeMode) return;
        mc.player.abilities.flying = false;
    }

    private enum ElytraFlightMode {
        BOOST, FLY,
    }

}
