package me.zeroeightsix.kami.module.modules.player;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.events.PacketEvent;
import me.zeroeightsix.kami.mixin.client.IMinecraftClient;
import me.zeroeightsix.kami.module.Module;
import net.minecraft.client.network.packet.PlaySoundS2CPacket;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvents;

/**
 * Created by 086 on 22/03/2018.
 */
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
                ((IMinecraftClient) mc).callDoAttack();
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                ((IMinecraftClient) mc).callDoAttack();
            }).start();
        }
    });

}
