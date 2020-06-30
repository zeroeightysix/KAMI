package me.zeroeightsix.kami.mixin.client;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.util.InputUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(InputUtil.Type.class)
public interface IInputUtilType {

    @Accessor
    Int2ObjectMap<InputUtil.KeyCode> getMap();

}
