package me.zeroeightsix.kami.event.events

import me.zeroeightsix.kami.event.KamiEvent
import net.minecraft.client.render.BufferBuilder
import net.minecraft.client.render.Tessellator
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.Vec3d

/**
 * Created by 086 on 10/12/2017.
 * https://github.com/fr1kin/ForgeHax/blob/4697e629f7fa4f85faa66f9ac080573407a6d078/src/main/java/com/matt/forgehax/events/RenderEvent.java
 */
open class RenderEvent private constructor(private val stage: Stage) : KamiEvent() {

    enum class Stage {
        WORLD, SCREEN
    }

    class Screen : RenderEvent(Stage.SCREEN)
    class World(
        val tessellator: Tessellator,
        val matrixStack: MatrixStack
    ) :
        RenderEvent(Stage.WORLD) {

        val buffer: BufferBuilder
            get() = tessellator.buffer

        init {
            era = Era.POST
        }
    }

}
