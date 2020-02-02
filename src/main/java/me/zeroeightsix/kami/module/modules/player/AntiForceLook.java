package me.zeroeightsix.kami.module.modules.player;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.events.PacketEvent;
import me.zeroeightsix.kami.mixin.client.IPlayerPositionLookS2CPacket;
import me.zeroeightsix.kami.module.Module;
import net.minecraft.client.network.packet.PlayerPositionLookS2CPacket;

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
