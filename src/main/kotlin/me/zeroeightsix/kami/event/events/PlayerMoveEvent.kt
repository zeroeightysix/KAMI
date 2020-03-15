package me.zeroeightsix.kami.event.events

import me.zeroeightsix.kami.event.KamiEvent
import net.minecraft.entity.MovementType
import net.minecraft.util.math.Vec3d

/**
 * @author 086
 */
class PlayerMoveEvent(val type: MovementType, val vec: Vec3d) : KamiEvent()