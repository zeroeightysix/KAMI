package me.zeroeightsix.kami.mixin.client;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.event.events.PacketEvent;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.PacketListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public class MixinClientConnection {

    @Inject(method = "handlePacket", at = @At("HEAD"), cancellable = true)
    private static void handlePacket(Packet<?> packet, PacketListener listener, CallbackInfo info) {
        PacketEvent.Receive receive = new PacketEvent.Receive(packet);
        KamiMod.EVENT_BUS.post(receive);
        if (receive.isCancelled())
            info.cancel();
    }

    @Inject(method = "sendImmediately", at = @At("HEAD"), cancellable = true)
    private void sendImmediately(Packet<?> packet, GenericFutureListener<? extends Future<? super Void>> listener, CallbackInfo info) {
        PacketEvent.Send send = new PacketEvent.Send(packet);
        KamiMod.EVENT_BUS.post(send);
        if (send.isCancelled())
            info.cancel();
    }

}
