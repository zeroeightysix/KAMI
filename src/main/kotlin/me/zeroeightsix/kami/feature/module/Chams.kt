package me.zeroeightsix.kami.feature.module

import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.EntityUtil
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity

/**
 * Created by 086 on 12/12/2017.
 */
@Module.Info(
    name = "Chams",
    category = Module.Category.RENDER,
    description = "See entities through walls"
)
object Chams : Module() {
    private val players =
        Settings.b("Players", true)
    private val animals =
        Settings.b("Animals", false)
    private val mobs =
        Settings.b("Mobs", false)

    fun renderChams(entity: Entity?): Boolean {
        return if (entity is PlayerEntity) players.value else if (EntityUtil.isPassive(entity)) animals.value else mobs.value
    }

    init {
        registerAll(
            players,
            animals,
            mobs
        )
    }
}