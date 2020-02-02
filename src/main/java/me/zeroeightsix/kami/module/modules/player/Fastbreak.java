package me.zeroeightsix.kami.module.modules.player;

import me.zeroeightsix.kami.mixin.client.IMinecraftClient;
import me.zeroeightsix.kami.module.ModulePlay;

/**
 * @author 086
 */
@ModulePlay.Info(name = "Fastbreak", category = ModulePlay.Category.PLAYER, description = "Nullifies block hit delay")
public class Fastbreak extends ModulePlay {

    @Override
    public void onUpdate() {
        ((IMinecraftClient) mc.interactionManager).setItemUseCooldown(0);
    }

}
