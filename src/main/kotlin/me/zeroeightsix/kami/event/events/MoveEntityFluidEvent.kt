package me.zeroeightsix.kami.event.events

import me.zeroeightsix.kami.event.KamiEvent
import net.minecraft.entity.Entity
import net.minecraft.util.math.Vec3d

class MoveEntityFluidEvent(val entity: Entity, var movement: Vec3d) :
    KamiEvent()