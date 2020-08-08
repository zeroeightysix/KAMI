package me.zeroeightsix.kami.mixin.client;

import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.event.events.PacketEvent;
import net.minecraft.network.DecoderHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.IOException;

@Mixin(DecoderHandler.class)
public class MixinDecoderHandler {

    @Redirect(method = "decode", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/Packet;read(Lnet/minecraft/network/PacketByteBuf;)V"))
    void read(Packet<?> packet, PacketByteBuf buf) throws IOException {
        packet.read(buf);
        PacketEvent.Receive receive = new PacketEvent.Receive(packet);
        KamiMod.EVENT_BUS.post(receive);
    }

}
