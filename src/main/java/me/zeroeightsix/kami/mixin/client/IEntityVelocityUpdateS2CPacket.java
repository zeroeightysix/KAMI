package me.zeroeightsix.kami.mixin.client;

import net.minecraft.client.network.packet.EntityVelocityUpdateS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityVelocityUpdateS2CPacket.class)
public interface IEntityVelocityUpdateS2CPacket {

    @Accessor
    int getID();
    @Accessor
    void setID(int id);

    @Accessor
    int getVelocityX();
    @Accessor
    int getVelocityY();
    @Accessor
    int getVelocityZ();

    @Accessor
    void setVelocityX(int x);
    @Accessor
    void setVelocityY(int y);
    @Accessor
    void setVelocityZ(int z);

}
