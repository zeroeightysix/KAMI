package me.zeroeightsix.kami.module.modules.movement;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.events.PacketEvent;
import me.zeroeightsix.kami.mixin.client.IPlayerMoveC2SPacket;
import me.zeroeightsix.kami.module.Module;
import net.minecraft.server.network.packet.PlayerMoveC2SPacket;

/**
 * Created by 086 on 8/04/2018.
 */
@Module.Info(name = "AntiHunger", category = Module.Category.MOVEMENT, description = "Lose hunger less fast. Might cause ghostblocks.")
public class AntiHunger extends Module {

    @EventHandler
    public Listener<PacketEvent.Send> packetListener = new Listener<>(event -> {
        if (event.getPacket() instanceof PlayerMoveC2SPacket) {
            ((IPlayerMoveC2SPacket) event.getPacket()).setOnGround(false);
        }
    });

}
