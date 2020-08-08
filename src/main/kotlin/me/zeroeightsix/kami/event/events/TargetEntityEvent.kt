package me.zeroeightsix.kami.event.events

import me.zeroeightsix.kami.event.KamiEvent
import net.minecraft.entity.Entity
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import java.util.function.Predicate

class TargetEntityEvent(
    val entity: Entity,
    val vec3d: Vec3d,
    val vec3d2: Vec3d,
    val box: Box,
    val predicate: Predicate<Entity>,
    val d: Double,
    var trace: EntityHitResult?
) : KamiEvent()
