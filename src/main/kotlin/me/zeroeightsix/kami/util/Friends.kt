package me.zeroeightsix.kami.util

import com.mojang.authlib.GameProfile
import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting
import io.github.fablabsmc.fablabs.api.fiber.v1.tree.PropertyMirror
import me.zeroeightsix.kami.feature.FindSettings

@FindSettings
object Friends {

    lateinit var mirror: PropertyMirror<MutableList<GameProfile>>

    @Setting
    var friends = mutableListOf<GameProfile>()

    @JvmStatic
    fun isFriend(name: String?): Boolean {
        return friends.stream().anyMatch {
            it.name.equals(name, ignoreCase = true)
        }
    }

    fun addFriend(profile: GameProfile) {
        if (!isFriend(profile.name)) {
            val list = friends.toMutableList()
            list.add(profile)
            mirror.value = list
        }
    }

    fun removeFriend(profile: GameProfile) {
        if (isFriend(profile.name)) {
            val list = friends.toMutableList()
            list.removeIf { it == profile }
            mirror.value = list
        }
    }

}
