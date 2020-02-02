package me.zeroeightsix.kami.module.modules.player;

import me.zeroeightsix.kami.mixin.client.IMinecraftClient;
import me.zeroeightsix.kami.module.Module;

/**
 * @author 086
 */
@Module.Info(name = "Fastbreak", category = Module.Category.PLAYER, description = "Nullifies block hit delay")
public class Fastbreak extends Module {

    @Override
    public void onUpdate() {
        ((IMinecraftClient) mc.interactionManager).setItemUseCooldown(0);
    }

}
