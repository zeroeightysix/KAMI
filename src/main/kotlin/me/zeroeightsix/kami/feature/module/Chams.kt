package me.zeroeightsix.kami.feature.module

import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting
import me.zeroeightsix.kami.util.EntityUtil
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity

@Module.Info(
    name = "Chams",
    category = Module.Category.RENDER,
    description = "See entities through walls"
)
object Chams : Module() {
    @Setting
    private var players = true

    @Setting
    private var animals = false

    @Setting
    private var mobs = false

    fun renderChams(entity: Entity?): Boolean {
        return if (entity is PlayerEntity) players else if (EntityUtil.isPassive(entity)) animals else mobs
    }
}
