package me.zeroeightsix.kami.module.modules.misc;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.event.events.ScreenEvent;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraft.client.gui.screen.DeathScreen;

/**
 * Created by 086 on 9/04/2018.
 * Updated 16 November 2019 by hub
 */
@Module.Info(name = "AutoRespawn", description = "Respawn utility", category = Module.Category.MISC)
public class AutoRespawn extends Module {

    private Setting<Boolean> respawn = register(Settings.b("Respawn", true));
    private Setting<Boolean> deathCoords = register(Settings.b("DeathCoords", false));
    private Setting<Boolean> antiGlitchScreen = register(Settings.b("Anti Glitch Screen", true));

    @EventHandler
    public Listener<ScreenEvent.Displayed> listener = new Listener<>(event -> {

        if (!(event.getScreen() instanceof DeathScreen)) {
            return;
        }

        if (deathCoords.getValue() && mc.player.getHealth() <= 0) {
            Command.sendChatMessage(String.format("You died at x %d y %d z %d", (int) mc.player.x, (int) mc.player.y, (int) mc.player.z));
        }

        if (respawn.getValue() || (antiGlitchScreen.getValue() && mc.player.getHealth() > 0)) {
            mc.player.requestRespawn();
            mc.openScreen(null);
        }

    });

}
