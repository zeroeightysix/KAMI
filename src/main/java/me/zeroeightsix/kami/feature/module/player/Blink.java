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
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayDeque;
import java.util.Queue;

@Module.Info(name = "Blink", category = Module.Category.PLAYER)
public class Blink extends Module {
    @Setting(comment = "Will not attempt to send packets if you are more than 10 blocks away from where you blinked")
    private boolean isCancelable = true;
    @Setting(comment = "Withhold all packets, not only Movement Packets")
    private boolean withholdAllPackets = true;
    private @Setting.Constrain.Range(min = 50d, max = 200d, step = Double.MIN_VALUE) int maxPacketAmount = 100;

    private OtherClientPlayerEntity clonedPlayer; // Fake Player when player blinked
    Queue<Packet> packets = new ArrayDeque<>(); // Create list to hold our packets

    @EventHandler
    public Listener<PacketEvent.Send> listener = new Listener<>(event -> {
        // We don't want to withhold login packets if a player logs out with blink enabled
        if (event.getPacket() instanceof PlayerMoveC2SPacket || (withholdAllPackets && mc.world != null && !(event.getPacket() instanceof PacketListener))) { // maybe ServerPlayPacketListener
            event.cancel();
            packets.add(event.getPacket());

            // Create a sense of urgency to unblink when approaching packet threshold
            Formatting packetColor = Formatting.WHITE;
            if (packets.size() > maxPacketAmount*(0.6f)) packetColor = Formatting.YELLOW;
            if (packets.size() > maxPacketAmount) packetColor = Formatting.RED;

            Formatting distanceColor = Formatting.WHITE;
            if (clonedPlayer.getPos().distanceTo(mc.player.getPos()) > 6)  distanceColor = Formatting.YELLOW;
            if (clonedPlayer.getPos().distanceTo(mc.player.getPos()) > 10) distanceColor = Formatting.RED;

            // Shows up above hotbar
            mc.player.sendMessage(Texts.f(Formatting.WHITE, Texts.append(
                    Texts.lit("Packets "),
                    Texts.flit(packetColor, Integer.toString(packets.size())),
                    Texts.flit(distanceColor, "/"+(withholdAllPackets?Integer.toString(maxPacketAmount):"∞")))),
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
        if (packets.size() > maxPacketAmount &&
                isCancelable && clonedPlayer != null &&
                clonedPlayer.getPos().distanceTo(mc.player.getPos()) > 10) { // If its more than 10 blocks

            System.out.println("Based and Redpilled");
            if (clonedPlayer != null) { // We don't want a NPE when a player tries to disable blink after logging in
                mc.player.setPos(clonedPlayer.getX(), clonedPlayer.getY(), clonedPlayer.getZ()); // Snap back to where we were
            }
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
