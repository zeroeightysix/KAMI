package me.zeroeightsix.kami.mixin.client;

import io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.derived.ConfigType;
import io.github.fablabsmc.fablabs.impl.fiber.annotation.BackedConfigLeaf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BackedConfigLeaf.class)
public interface IBackedConfigLeaf<R, S> {

    @Accessor
    ConfigType<R, S, ?> getType();


}
