package me.zeroeightsix.kami.command.commands;

import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.command.syntax.ChunkBuilder;
import me.zeroeightsix.kami.command.syntax.parsers.ModuleParser;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.ModuleManager;
import me.zeroeightsix.kami.setting.Named;
import me.zeroeightsix.kami.setting.Setting;

import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by 086 on 18/11/2017.
 */
public class SetCommand extends Command {

    public SetCommand() {
        super("set", new ChunkBuilder()
                .append("module", true, new ModuleParser())
                .append("setting", true)
                .append("value", true)
                .build());
    }

    @Override
    public void call(String[] args) {
        if (args[0] == null) {
            Command.sendChatMessage("Please specify a module!");
            return;
        }

        Module m = ModuleManager.getModuleByName(args[0]);
        if (m == null) {
            Command.sendChatMessage("Unknown module &b" + args[0] + "&r!");
            return;
        }

        if (args[1] == null) {
            String settings = String.join(", ",  m.settingList.stream().filter(setting -> setting instanceof Named).map(setting -> ((Named) setting).getName()).collect(Collectors.toList()));
            if (settings.isEmpty())
                Command.sendChatMessage("Module &b" + m.getName() + "&r has no settings.");
            else {
                Command.sendStringChatMessage(new String[]{
                        "Please specify a setting! Choose one of the following:", settings
                });
            }
            return;
        }

        Optional<Setting> optionalSetting = m.settingList.stream().filter(setting1 -> setting1 instanceof Named).filter(setting1 -> ((Named) setting1).getName().equalsIgnoreCase(args[1])).findFirst();
        if (!optionalSetting .isPresent()) {
            Command.sendChatMessage("Unknown setting &b" + args[1] + "&r in &b" + m.getName() + "&r!");
            return;
        }

        Setting setting = optionalSetting.get();

        if (args[2] == null) {
            Command.sendChatMessage("&b" + ((Named) setting).getName() + "&r is a &3" + setting.getValue().getClass().getSimpleName() + "&r. Its current value is &3" + setting.getValue());
            return;
        }

        try {
            setting.setValue(args[2]);
            Command.sendChatMessage("Set &b" + ((Named) setting).getName() + "&r to &3" + args[2] + "&r.");
        } catch (Exception e) {
            e.printStackTrace();
            Command.sendChatMessage("Unable to set value! &6" + e.getMessage());
        }
    }
}
