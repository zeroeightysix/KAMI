package me.zeroeightsix.kami.feature.module.render;

import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting;
import me.zeroeightsix.kami.feature.command.Command;
import me.zeroeightsix.kami.feature.module.Module;
import me.zeroeightsix.kami.util.Friends;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.LiteralText;

/**
 * Created by 086 on 8/04/2018.
 */
@Module.Info(name = "ExtraTab", description = "Expands the player tab menu", category = Module.Category.RENDER)
public class ExtraTab extends Module {

    @Setting
    public @Setting.Constrain.Range(min = 0.0) int tabSize = 80;

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
