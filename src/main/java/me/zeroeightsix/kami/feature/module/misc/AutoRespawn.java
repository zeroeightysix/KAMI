package me.zeroeightsix.kami.feature.module.misc;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.events.ScreenEvent;
import me.zeroeightsix.kami.feature.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
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

    private Setting<Boolean> respawn = register(Settings.b("Respawn", true));
    private Setting<Boolean> deathCoords = register(Settings.b("DeathCoords", false));
    private Setting<Boolean> antiGlitchScreen = register(Settings.b("Anti Glitch Screen", true));

    @EventHandler
    public Listener<ScreenEvent.Displayed> listener = new Listener<>(event -> {

        if (!(event.getScreen() instanceof DeathScreen)) {
            return;
        }

        if (respawn.getValue() || (antiGlitchScreen.getValue() && mc.player.getHealth() > 0)) {
            if (deathCoords.getValue()) {
                Calendar cal = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

                Wrapper.getPlayer().sendMessage(Texts.f(Formatting.GOLD, Texts.append(
                        Texts.lit("You died at "),
                        Texts.flit(Formatting.YELLOW, "x " + Math.floor(mc.player.x)),
                        Texts.lit(", "),
                        Texts.flit(Formatting.YELLOW, "y " + Math.floor(mc.player.y)),
                        Texts.lit(", "),
                        Texts.flit(Formatting.YELLOW, "z " + Math.floor(mc.player.z)),
                        Texts.lit(" ("),
                        Texts.flit(Formatting.AQUA, sdf.format(cal.getTime())),
                        Texts.lit(").")
                )));
            }
            mc.player.requestRespawn();
            mc.openScreen(null);
        }

    });

}
