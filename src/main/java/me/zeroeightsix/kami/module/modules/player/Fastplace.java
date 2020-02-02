package me.zeroeightsix.kami.module.modules.player;

import me.zeroeightsix.kami.mixin.client.IMinecraftClient;
import me.zeroeightsix.kami.module.ModulePlay;

/**
 * @author 086
 */
@ModulePlay.Info(name = "Fastplace", category = ModulePlay.Category.PLAYER, description = "Nullifies block place delay")
public class Fastplace extends ModulePlay {

    @Override
    public void onUpdate() {
        ((IMinecraftClient) mc).setItemUseCooldown(0);
    }

}
