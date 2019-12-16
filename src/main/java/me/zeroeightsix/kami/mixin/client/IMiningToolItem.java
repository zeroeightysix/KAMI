package me.zeroeightsix.kami.mixin.client;

import net.minecraft.item.MiningToolItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MiningToolItem.class)
public interface IMiningToolItem {

    @Accessor
    float getAttackDamage();

}
