package me.zeroeightsix.kami.mixin.client;

import net.minecraft.client.network.packet.ChatMessageS2CPacket;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChatMessageS2CPacket.class)
public interface IChatMessageS2CPacket {

    @Accessor
    void setMessage(Text text);
    @Accessor
    Text getMessage();

}
