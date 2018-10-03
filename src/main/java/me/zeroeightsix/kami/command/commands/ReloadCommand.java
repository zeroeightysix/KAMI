package me.zeroeightsix.kami.command.commands;

import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.command.syntax.ChunkBuilder;
import me.zeroeightsix.kami.gui.old.kami.KamiGUI;
import me.zeroeightsix.kami.setting.SettingsPool;

import java.io.File;

/**
 * Created by 086 on 26/12/2017.
 */
public class ReloadCommand extends Command {

    public ReloadCommand() {
        super("reload", new ChunkBuilder().build());
    }

    @Override
    public void call(String[] args) {
        KamiMod.getInstance().guiManager = new KamiGUI();
        KamiMod.getInstance().guiManager.initializeGUI();
        File file = new File("kami.settings");
        if (file.exists()) {
            try {
                SettingsPool.save(file);
                SettingsPool.load(file);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
