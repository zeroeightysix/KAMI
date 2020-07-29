package me.zeroeightsix.kami.mixin.client;

import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerMoveC2SPacket.class)
public interface IPlayerMoveC2SPacket {

    @Accessor
    boolean getOnGround();
    @Accessor
    void setOnGround(boolean onGround);
    @Accessor
    double getX();
    @Accessor
    double getY();
    @Accessor
    double getZ();
    @Accessor
    void setX(double x);
    @Accessor
    void setY(double y);
    @Accessor
    void setZ(double z);
    @Accessor
    void setYaw(float yaw);
    @Accessor
    void setPitch(float pitch);

}
