package me.zeroeightsix.kami.module.modules.player;

import me.zeroeightsix.kami.mixin.client.IMinecraftClient;
import me.zeroeightsix.kami.module.Module;

/**
 * @author 086
 */
@Module.Info(name = "Fastplace", category = Module.Category.PLAYER, description = "Nullifies block place delay")
public class Fastplace extends Module {

    @Override
    public void onUpdate() {
        ((IMinecraftClient) mc).setItemUseCooldown(0);
    }

}
