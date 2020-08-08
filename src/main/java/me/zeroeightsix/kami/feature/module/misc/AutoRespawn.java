package me.zeroeightsix.kami.feature.module.misc;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting;
import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Settings;
import me.zeroeightsix.kami.event.events.ScreenEvent;
import me.zeroeightsix.kami.feature.module.Module;
import me.zeroeightsix.kami.util.Texts;
import me.zeroeightsix.kami.util.Wrapper;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.util.Formatting;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by 086 on 9/04/2018.
 * Updated 16 November 2019 by hub
 */
@Module.Info(name = "AutoRespawn", description = "Respawn utility", category = Module.Category.MISC)
public class AutoRespawn extends Module {

    @Setting(name = "Respawn")
    private boolean respawn = true;
    @Setting(name = "DeathCoords")
    private boolean deathCoords = false;
    @Setting(name = "Anti Glitch Screen")
    private boolean antiGlitchScreen = true;

    @EventHandler
    public Listener<ScreenEvent.Displayed> listener = new Listener<>(event -> {

        if (!(event.getScreen() instanceof DeathScreen)) {
            return;
        }

        if (respawn || (antiGlitchScreen && mc.player.getHealth() > 0)) {
            if (deathCoords) {
                Calendar cal = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

                Wrapper.getPlayer().sendSystemMessage(Texts.f(Formatting.GOLD, Texts.append(
                        Texts.lit("You died at "),
                        Texts.flit(Formatting.YELLOW, "x " + Math.floor(mc.player.getX())),
                        Texts.lit(", "),
                        Texts.flit(Formatting.YELLOW, "y " + Math.floor(mc.player.getY())),
                        Texts.lit(", "),
                        Texts.flit(Formatting.YELLOW, "z " + Math.floor(mc.player.getZ())),
                        Texts.lit(" ("),
                        Texts.flit(Formatting.AQUA, sdf.format(cal.getTime())),
                        Texts.lit(").")
                )), null);
            }
            mc.player.requestRespawn();
            mc.openScreen(null);
        }

    });

}
