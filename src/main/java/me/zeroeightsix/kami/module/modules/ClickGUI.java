package me.zeroeightsix.kami.module.modules;

import me.zeroeightsix.kami.gui.old.kami.DisplayGuiScreen;
import me.zeroeightsix.kami.module.Module;
import org.lwjgl.input.Keyboard;

/**
 * Created by 086 on 23/08/2017.
 */
@Module.Info(name = "clickGUI", description = "Opens the Click GUI", bind = Keyboard.KEY_Y, category = Module.Category.HIDDEN)
public class ClickGUI extends Module {

    @Override
    protected void onEnable() {
        if (!(mc.currentScreen instanceof DisplayGuiScreen)) {
            mc.displayGuiScreen(new DisplayGuiScreen(mc.currentScreen));
        }
        disable();
    }

}
