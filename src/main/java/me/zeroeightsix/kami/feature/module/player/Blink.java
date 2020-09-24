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
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerSpawnS2CPacket;
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
    @Setting(comment = "How many packets are allowed to be cached before they all get canceled")
    private @Setting.Constrain.Range(min = 20d, max = 200d, step = Double.MIN_VALUE) int maxPacketAmount = 50;

    @Setting(comment = "Send packets gradually instead of all at once")
    private boolean gradualPacketMode = false;
    @Setting(comment = "The percent of cached packets to send per tick")
    private @Setting.Constrain.Range(min = 1d, max = 100d, step = Double.MIN_VALUE) int percentOfPacketsPerTick = 50;

    private OtherClientPlayerEntity clonedPlayer; // Fake Player when player blinked
    Queue<Packet> packets = new ArrayDeque<>(); // Create list to hold our packets

    @EventHandler
    public Listener<PacketEvent.Send> listener = new Listener<>(event -> {
        // We don't want to withhold login packets if a player logs out with blink enabled
        if (event.getPacket() instanceof PlayerMoveC2SPacket || (withholdAllPackets && mc.world != null && !(event.getPacket() instanceof PlayerRespawnS2CPacket) && !(event.getPacket() instanceof PlayerSpawnS2CPacket) && !(event.getPacket() instanceof PacketListener))) { // maybe use ServerPlayPacketListener
            event.cancel();
            packets.add(event.getPacket());

            // Create a sense of urgency to unblink when approaching packet threshold
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
            mc.world.addEntity(-68419, clonedPlayer); //There is likely not going to be an entity with this id
        }
    }

    @Override
    public void onDisable() {
        if (packets.size() > maxPacketAmount && isCancelable && clonedPlayer != null) {
            if (clonedPlayer != null)  // We don't want a NPE when a player tries to disable blink after logging in
                mc.player.updatePosition(clonedPlayer.getX(), clonedPlayer.getY(), clonedPlayer.getZ()); // Snap back to where we were
            packets.clear(); //Empty the list of packets to send
        }

        if (!gradualPacketMode) while (!packets.isEmpty()) mc.getNetworkHandler().sendPacket(packets.poll()); // Send all packets at once
        else {
            float packetsPerClump = packets.size()*(percentOfPacketsPerTick/100f); // Get the amount of packets to send
            Thread sendPacketThread = new Thread(() -> { // Use a new thread because the while loop might take some time to finish
                while (!packets.isEmpty() && mc.player != null)
                    if (mc.player.age % 2 == 0) // Runs every tick
                        for (int i = 0; i < packetsPerClump; i++)
                            if (!packets.isEmpty())
                                mc.getNetworkHandler().sendPacket(packets.poll());
            });
            sendPacketThread.start();
        }


        PlayerEntity localPlayer = mc.player;
        if (localPlayer != null) {
            mc.world.removeEntity(-68419); // Remove fake blink Player
            clonedPlayer = null;
        }
    }

}
