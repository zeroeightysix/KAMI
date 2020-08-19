package me.zeroeightsix.kami.mixin.extend

import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import me.zeroeightsix.kami.mixin.client.ICamera
import me.zeroeightsix.kami.mixin.client.IGameRenderer
import me.zeroeightsix.kami.mixin.client.IInputUtilType
import me.zeroeightsix.kami.mixin.client.IMatrixStack
import net.minecraft.client.input.Input
import net.minecraft.client.render.Camera
import net.minecraft.client.render.GameRenderer
import net.minecraft.client.util.InputUtil
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.Vec3d
import java.util.*

fun InputUtil.Type.getMap(): Int2ObjectMap<InputUtil.Key> = (this as IInputUtilType).map

fun MatrixStack.getStack(): Deque<MatrixStack.Entry> = (this as IMatrixStack).stack

fun Camera.setPos(pos: Vec3d) = (this as ICamera).callSetPos(pos)
fun Camera.setRotation(yaw: Float, pitch: Float) = (this as ICamera).callSetRotation(yaw, pitch)

fun Input.update(from: Input) = (this as ExtendedInput).update(from)

fun GameRenderer.setRenderHand(renderHand: Boolean) = (this as IGameRenderer).setRenderHand(renderHand)
