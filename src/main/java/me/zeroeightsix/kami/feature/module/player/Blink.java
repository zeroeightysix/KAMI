package me.zeroeightsix.kami.feature.module.player;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.events.PacketEvent;
import me.zeroeightsix.kami.feature.module.Module;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by 086 on 24/01/2018.
 * Edited by Cuhnt on 30/7/2019
 */
@Module.Info(name = "Blink", category = Module.Category.PLAYER)
public class Blink extends Module {

    Queue<PlayerMoveC2SPacket> packets = new LinkedList<>();
    @EventHandler
    public Listener<PacketEvent.Send> listener = new Listener<>(event -> {
        if (isEnabled() && event.getPacket() instanceof PlayerMoveC2SPacket) {
            event.cancel();
            packets.add((PlayerMoveC2SPacket) event.getPacket());
        }
    });
    private OtherClientPlayerEntity clonedPlayer;

    @Override
    public void onEnable() {
        if (mc.player != null) {
            clonedPlayer = new OtherClientPlayerEntity(mc.world, mc.getSession().getProfile());
            clonedPlayer.copyFrom(mc.player);
            clonedPlayer.headYaw = mc.player.headYaw;
            mc.world.addEntity(-100, clonedPlayer);
        }
    }

    @Override
    public void onDisable() {
        while (!packets.isEmpty())
            mc.getNetworkHandler().sendPacket(packets.poll());

        PlayerEntity localPlayer = mc.player;
        if (localPlayer != null) {
            mc.world.removeEntity(-100);
            clonedPlayer = null;
        }
    }

}
