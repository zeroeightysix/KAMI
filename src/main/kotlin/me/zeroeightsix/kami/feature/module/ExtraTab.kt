package me.zeroeightsix.kami.feature.module

import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting
import me.zeroeightsix.kami.util.Friends.isFriend
import net.minecraft.client.network.PlayerListEntry
import net.minecraft.scoreboard.Team
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import net.minecraft.util.Formatting

@Module.Info(name = "ExtraTab", description = "Expands the player tab menu", category = Module.Category.RENDER)
object ExtraTab : Module() {
    @JvmField
    @Setting
    var tabSize = 80

    @Setting
    var highlightFriends = true

    @JvmStatic
    fun getPlayerName(networkPlayerInfoIn: PlayerListEntry): Text? {
        val dname =
            if (networkPlayerInfoIn.displayName != null) networkPlayerInfoIn.displayName else Team.modifyText(
                networkPlayerInfoIn.scoreboardTeam,
                LiteralText(networkPlayerInfoIn.profile.name)
            )
        return if (highlightFriends && isFriend(dname!!.string)) dname.shallowCopy()
            .formatted(Formatting.GREEN) else dname
    }

}
