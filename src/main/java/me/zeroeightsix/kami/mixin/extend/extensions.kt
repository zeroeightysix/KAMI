package me.zeroeightsix.kami.mixin.extend

import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import me.zeroeightsix.kami.mixin.client.IInputUtilType
import me.zeroeightsix.kami.mixin.client.IMatrixStack
import me.zeroeightsix.kami.mixin.client.`IMatrixStack$Entry`
import net.minecraft.client.render.GameRenderer
import net.minecraft.client.util.InputUtil
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.Matrix3f
import net.minecraft.util.math.Matrix4f
import java.util.*

fun InputUtil.Type.getMap(): Int2ObjectMap<InputUtil.Key> = (this as IInputUtilType).map

fun MatrixStack.getStack(): Deque<MatrixStack.Entry> = (this as IMatrixStack).stack
