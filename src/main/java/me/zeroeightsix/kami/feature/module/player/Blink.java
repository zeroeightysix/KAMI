package me.zeroeightsix.kami.feature.module.player;

import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.PacketEvent;
import me.zeroeightsix.kami.feature.module.Module;
import me.zeroeightsix.kami.util.Texts;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Formatting;

import java.util.LinkedList;
import java.util.Queue;


@Module.Info(name = "Blink", category = Module.Category.PLAYER)
public class Blink extends Module {
    @Setting
    private boolean packetDiscard = true;
    @Setting(comment = "Withhold all packets, not just PlayerMoveC2SPacket")
    private boolean withholdAllPackets = true;
    @Setting
    private @Setting.Constrain.Range(min = 50, max = 500, step = Double.MIN_VALUE) int maxPacketAmount = 100;

    private OtherClientPlayerEntity clonedPlayer; // Fake Player when player blinked
    Queue<Packet> packets = new LinkedList<>(); // Create list to hold our packets

    @EventHandler
    public Listener<PacketEvent.Send> listener = new Listener<>(event -> {
        // We don't want to withhold login packets if a player logs out with blink enabled
        if (event.getPacket() instanceof PlayerMoveC2SPacket || (withholdAllPackets && mc.world != null)) {
            event.cancel();
            packets.add(event.getPacket());

            // Create a sense of urgency to unblick when approaching packet threshold
            Formatting packetColor = Formatting.WHITE;
            if (packets.size() > maxPacketAmount*(0.6f)) packetColor = Formatting.YELLOW;
            if (packets.size() > maxPacketAmount) packetColor = Formatting.RED;

            // Shows up above hotbar
            mc.player.sendMessage(Texts.f(Formatting.WHITE, Texts.append(
                    Texts.lit("Packets "),
                    Texts.flit(packetColor, Integer.toString(packets.size())),
                    Texts.flit(Formatting.WHITE, "/"+(withholdAllPackets?Integer.toString(maxPacketAmount):"∞")))),
                    true);
        }
    });

    @Override
    public void onEnable() {
        if (mc.player != null) {
            clonedPlayer = new OtherClientPlayerEntity(mc.world, mc.getSession().getProfile()); // Create Fake Player
            clonedPlayer.copyFrom(mc.player);
            clonedPlayer.headYaw = mc.player.headYaw;
            // id of cloned player is -68419 in the future there might be an entity with an id -69420
            // but nobody is going to use id -68419
            mc.world.addEntity(-68419, clonedPlayer);
        }
    }

    @Override
    public void onDisable() {
        if (packets.size() > maxPacketAmount && packetDiscard) {
            if (clonedPlayer != null) // We don't want a NPE when a player tries to disable blink after logging in
                mc.player.setPos(clonedPlayer.getPos().x, clonedPlayer.getPos().y, clonedPlayer.getPos().z); // Snap back to where we were
            packets.clear(); //Empty the list of packets to send
        }

        while (!packets.isEmpty()) mc.getNetworkHandler().sendPacket(packets.poll()); // Send all packets at once

        PlayerEntity localPlayer = mc.player;
        if (localPlayer != null) {
            mc.world.removeEntity(-68419); // Remove fake blink Player
            clonedPlayer = null;
        }
    }

}
