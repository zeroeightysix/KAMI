package me.zeroeightsix.kami.feature.module.player;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.PacketEvent;
import me.zeroeightsix.kami.feature.module.Module;
import me.zeroeightsix.kami.mixin.client.IMinecraftClient;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.sound.SoundEvents;

@Module.Info(name = "AutoFish", category = Module.Category.MISC, description = "Automatically catch fish")
public class AutoFish extends Module {

    @EventHandler
    private Listener<PacketEvent.Receive> receiveListener = new Listener<>(event -> {
        if (mc.player != null && (mc.player.getMainHandStack().getItem() == Items.FISHING_ROD || mc.player.getOffHandStack().getItem() == Items.FISHING_ROD) && event.getPacket() instanceof PlaySoundS2CPacket && SoundEvents.ENTITY_FISHING_BOBBER_SPLASH.equals(((PlaySoundS2CPacket) event.getPacket()).getSound())) {
            new Thread(() -> {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                ((IMinecraftClient) mc).callDoItemUse();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                ((IMinecraftClient) mc).callDoItemUse();
            }).start();
        }
    });

}
