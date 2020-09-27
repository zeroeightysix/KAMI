package me.zeroeightsix.kami.util

import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.render.FixedColorVertexConsumer
import net.minecraft.client.render.VertexConsumer

@Environment(EnvType.CLIENT)
class OutlineVertexConsumer(
    private val delegate: VertexConsumer,
    i: Int,
    j: Int,
    k: Int,
    l: Int
) : FixedColorVertexConsumer() {
    private var x = 0.0
    private var y = 0.0
    private var z = 0.0
    private var u = 0f
    private var v = 0f
    override fun fixedColor(i: Int, j: Int, k: Int, l: Int) {}
    override fun vertex(d: Double, e: Double, f: Double): VertexConsumer {
        x = d
        y = e
        z = f
        return this
    }

    override fun color(i: Int, j: Int, k: Int, l: Int): VertexConsumer {
        return this
    }

    override fun texture(f: Float, g: Float): VertexConsumer {
        u = f
        v = g
        return this
    }

    override fun overlay(i: Int, j: Int): VertexConsumer {
        return this
    }

    override fun light(i: Int, j: Int): VertexConsumer {
        return this
    }

    override fun normal(f: Float, g: Float, h: Float): VertexConsumer {
        return this
    }

    override fun vertex(
        f: Float,
        g: Float,
        h: Float,
        i: Float,
        j: Float,
        k: Float,
        l: Float,
        m: Float,
        n: Float,
        o: Int,
        p: Int,
        q: Float,
        r: Float,
        s: Float
    ) {
        delegate.vertex(f.toDouble(), g.toDouble(), h.toDouble()).color(fixedRed, fixedGreen, fixedBlue, fixedAlpha)
            .texture(m, n).next()
    }

    override fun next() {
        delegate.vertex(x, y, z).color(fixedRed, fixedGreen, fixedBlue, fixedAlpha).texture(u, v).next()
    }

    init {
        super.fixedColor(i, j, k, l)
    }
}