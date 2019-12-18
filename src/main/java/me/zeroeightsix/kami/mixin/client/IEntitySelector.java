package me.zeroeightsix.kami.mixin.client;

import net.minecraft.command.EntitySelector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.UUID;

@Mixin(EntitySelector.class)
public interface IEntitySelector {

    @Accessor
    String getPlayerName();
    @Accessor
    UUID getUuid();

}
