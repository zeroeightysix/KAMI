package me.zeroeightsix.kami.mixin.client;

import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ExplosionS2CPacket.class)
public interface IExplosionS2CPacket {

    @Accessor
    float getPlayerVelocityX();

    @Accessor
    void setPlayerVelocityX(float playerVelocityX);

    @Accessor
    float getPlayerVelocityY();

    @Accessor
    void setPlayerVelocityY(float playerVelocityY);

    @Accessor
    float getPlayerVelocityZ();

    @Accessor
    void setPlayerVelocityZ(float playerVelocityZ);

}
