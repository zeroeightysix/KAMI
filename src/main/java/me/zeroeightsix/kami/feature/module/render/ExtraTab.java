package me.zeroeightsix.kami.feature.module.render;

import me.zeroeightsix.kami.feature.command.Command;
import me.zeroeightsix.kami.feature.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.util.Friends;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.LiteralText;

/**
 * Created by 086 on 8/04/2018.
 */
@Module.Info(name = "ExtraTab", description = "Expands the player tab menu", category = Module.Category.RENDER)
public class ExtraTab extends Module {

    public Setting<Integer> tabSize = register(Settings.integerBuilder("Players").withMinimum(1).withValue(80).build());

    public static ExtraTab INSTANCE;

    public ExtraTab() {
        ExtraTab.INSTANCE = this;
    }

    public static String getPlayerName(PlayerListEntry networkPlayerInfoIn) {
        String dname = networkPlayerInfoIn.getDisplayName() != null ? networkPlayerInfoIn.getDisplayName().getString() : Team.modifyText(networkPlayerInfoIn.getScoreboardTeam(), new LiteralText(networkPlayerInfoIn.getProfile().getName())).getString();
        if (Friends.isFriend(dname)) return String.format("%sa%s", Command.SECTION_SIGN, dname);
        return dname;
    }
}
