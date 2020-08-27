package me.zeroeightsix.kami.util

import me.zeroeightsix.kami.mc
import me.zeroeightsix.kami.multiplyMatrix
import me.zeroeightsix.kami.toScreen
import net.minecraft.util.math.Matrix4f
import net.minecraft.util.math.Quaternion
import net.minecraft.util.math.Vec2f
import net.minecraft.util.math.Vec3d

object VectorMath {

    @JvmStatic
    fun project3Dto2D(
        position: Vec3d,
        modelMatrix: Matrix4f,
        projectionMatrix: Matrix4f
    ): Vec2f? {
        // Let's treat Quaternion as a 4x1 for convenience

        val quaternion = Quaternion(
            position.x.toFloat(),
            position.y.toFloat(),
            position.z.toFloat(),
            1.0F
        )

        val out = projectionMatrix.multiplyMatrix(modelMatrix.multiplyMatrix(quaternion))

        // will be <0f if the coordinate was 'behind' the camera, thus returning a mirrored coordinate
        if (out.w <= 0.0f) {
            return null
        }

        val screen = out.toScreen()
        val x = screen.x * mc.window.width
        val y = screen.y * mc.window.height

        return if (x.isInfinite() || y.isInfinite()) {
            null
        } else Vec2f(x, y)
    }

    @JvmStatic
    fun divideVec2f(vec: Vec2f?, factor: Float): Vec2f? {
        if (vec == null) return null
        return Vec2f(vec.x / factor, vec.y / factor)
    }

}
