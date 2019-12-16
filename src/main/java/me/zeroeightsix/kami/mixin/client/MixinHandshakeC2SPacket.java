package me.zeroeightsix.kami.mixin.client;

import me.zeroeightsix.kami.module.ModuleManager;
import net.minecraft.network.NetworkState;
import net.minecraft.server.network.packet.HandshakeC2SPacket;
import net.minecraft.util.PacketByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Created by 086 on 9/04/2018.
 */
@Mixin(HandshakeC2SPacket.class)
public class MixinHandshakeC2SPacket {

    @Shadow
    private int version;
    @Shadow
    private String address;
    @Shadow
    private int port;
    @Shadow
    private NetworkState state;

    @Inject(method = "write", at = @At(value = "HEAD"), cancellable = true)
    public void writePacketData(PacketByteBuf buf, CallbackInfo info) {
        if (ModuleManager.isModuleEnabled("FakeVanilla")) {
            info.cancel();
            buf.writeVarInt(version);
            buf.writeString(address);
            buf.writeShort(port);
            buf.writeVarInt(state.getId());
        }
    }

}
