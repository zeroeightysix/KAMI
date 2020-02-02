package me.zeroeightsix.kami.module.modules.misc;

import me.zeroeightsix.kami.module.ModulePlay;

/**
 * @author 086
 * @see me.zeroeightsix.kami.mixin.client.MixinNetworkManager
 */
@ModulePlay.Info(name = "NoPacketKick", category = ModulePlay.Category.MISC, description = "Prevent large packets from kicking you")
public class NoPacketKick {
    private static NoPacketKick INSTANCE;

    public NoPacketKick() {
        INSTANCE = this;
    }

    public static boolean isEnabled() {
        return INSTANCE.isEnabled();
    }

}
