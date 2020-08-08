package me.zeroeightsix.kami.feature.module.player;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.events.PacketEvent;
import me.zeroeightsix.kami.mixin.client.IPlayerPositionLookS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import me.zeroeightsix.kami.feature.module.Module;

/**
 * Created by 086 on 12/12/2017.
 */
@Module.Info(name = "AntiForceLook", category = Module.Category.PLAYER)
public class AntiForceLook extends Module {

    @EventHandler
    Listener<PacketEvent.Receive> receiveListener = new Listener<>(event -> {
        if (mc.player == null) return;
        if (event.getPacket() instanceof PlayerPositionLookS2CPacket) {
            IPlayerPositionLookS2CPacket yawPitch = (IPlayerPositionLookS2CPacket) event.getPacket();
            yawPitch.setYaw(mc.player.yaw);
            yawPitch.setPitch(mc.player.pitch);
        }
    });

}
