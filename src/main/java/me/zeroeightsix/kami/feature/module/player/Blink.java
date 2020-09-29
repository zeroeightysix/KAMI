package me.zeroeightsix.kami.feature.module.player;

import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.PacketEvent;
import me.zeroeightsix.kami.feature.module.Module;
import me.zeroeightsix.kami.util.Texts;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Formatting;

import java.util.ArrayDeque;
import java.util.Objects;
import java.util.Queue;

@Module.Info(name = "Blink", category = Module.Category.PLAYER)
public class Blink extends Module {
    @Setting
    private boolean packetDiscard = true;
    @Setting(comment = "Withhold all packets, not only Movement Packets")
    private boolean withholdAllPackets = true;
    @Setting
    private @Setting.Constrain.Range(min = 50d, max = 500d) int maxPacketAmount = 100;

    private OtherClientPlayerEntity clonedPlayer; // Fake Player when player blinked
    Queue<Packet<?>> packets = new ArrayDeque<>(); // Create list to hold our packets

    @EventHandler
    public Listener<PacketEvent.Send> listener = new Listener<>(event -> {
        // We don't want to withhold login packets if a player logs out with blink enabled
        ClientPlayerEntity player = mc.player;
        if (player != null && (event.getPacket() instanceof PlayerMoveC2SPacket || (withholdAllPackets && mc.world != null))) {
            event.cancel();
            packets.add(event.getPacket());

            // Create a sense of urgency to unblick when approaching packet threshold
            Formatting packetColor = Formatting.WHITE;
            if (packets.size() > maxPacketAmount*(0.6f)) packetColor = Formatting.YELLOW;
            if (packets.size() > maxPacketAmount) packetColor = Formatting.RED;

            // Shows up above hotbar
            player.sendMessage(Texts.f(Formatting.WHITE, Texts.append(
                Texts.lit("Packets "),
                Texts.flit(packetColor, Integer.toString(packets.size())),
                Texts.flit(Formatting.WHITE, "/"+(withholdAllPackets?Integer.toString(maxPacketAmount):"∞")))),
                true);
        }
    });

    @Override
    public void onEnable() {
        ClientPlayerEntity player = mc.player;
        ClientWorld world = mc.world;
        if (player != null && world != null) {
            clonedPlayer = new OtherClientPlayerEntity(world, mc.getSession().getProfile()); // Create Fake Player
            clonedPlayer.copyFrom(player);
            clonedPlayer.headYaw = player.headYaw;
            // id of cloned player is -68419 in the future there might be an entity with an id -69420
            // but nobody is going to use id -68419
            world.addEntity(-68419, clonedPlayer);
        }
    }

    @Override
    public void onDisable() {
        ClientPlayerEntity player = mc.player;
        ClientWorld world = mc.world;

        if (packets.size() > maxPacketAmount && packetDiscard) {
            if (clonedPlayer != null && player != null) // We don't want a NPE when a player tries to disable blink after logging in
                player.setPos(clonedPlayer.getPos().x, clonedPlayer.getPos().y, clonedPlayer.getPos().z); // Snap back to where we were
            packets.clear(); //Empty the list of packets to send
        }

        while (!packets.isEmpty()) Objects.requireNonNull(mc.getNetworkHandler()).sendPacket(packets.poll()); // Send all packets at once

        if (player != null && world != null) {
            world.removeEntity(-68419); // Remove fake blink Player
            clonedPlayer = null;
        }
    }

}
