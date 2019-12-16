package me.zeroeightsix.kami.mixin.client;

import net.minecraft.client.network.packet.PlayerPositionLookS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerPositionLookS2CPacket.class)
public interface IPlayerPositionLookS2CPacket {

    @Accessor
    float getYaw();
    @Accessor
    float getPitch();

    @Accessor
    void setYaw(float yaw);
    @Accessor
    void setPitch(float pitch);

}
