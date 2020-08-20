package me.zeroeightsix.kami.feature.module.movement;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.PacketEvent;
import me.zeroeightsix.kami.feature.module.Module;
import me.zeroeightsix.kami.mixin.client.IPlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

@Module.Info(name = "AntiHunger", category = Module.Category.MOVEMENT, description = "Lose hunger less fast. Might cause ghostblocks.")
public class AntiHunger extends Module {

    @EventHandler
    public Listener<PacketEvent.Send> packetListener = new Listener<>(event -> {
        if (event.getPacket() instanceof PlayerMoveC2SPacket) {
            ((IPlayerMoveC2SPacket) event.getPacket()).setOnGround(false);
        }
    });

}
