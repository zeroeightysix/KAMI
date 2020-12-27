@file:Suppress("CAST_NEVER_SUCCEEDS")

package me.zeroeightsix.kami.mixin.extend

import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import me.zeroeightsix.kami.mixin.client.ICamera
import me.zeroeightsix.kami.mixin.client.IGameRenderer
import me.zeroeightsix.kami.mixin.client.IInputUtilType
import me.zeroeightsix.kami.mixin.client.IMatrix4f
import me.zeroeightsix.kami.mixin.client.IMatrixStack
import me.zeroeightsix.kami.mixin.client.IMinecraftClient
import net.minecraft.client.MinecraftClient
import net.minecraft.client.input.Input
import net.minecraft.client.render.Camera
import net.minecraft.client.render.GameRenderer
import net.minecraft.client.util.InputUtil
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.Matrix4f
import net.minecraft.util.math.Vec3d
import java.util.Deque

// TODO: These could be extension properties

fun MinecraftClient.openChatScreen(text: String) = (this as IMinecraftClient).callOpenChatScreen(text)

fun InputUtil.Type.getMap(): Int2ObjectMap<InputUtil.Key> = (this as IInputUtilType).map

fun MatrixStack.getStack(): Deque<MatrixStack.Entry> = (this as IMatrixStack).stack

// `i` prefix as in 'interface'. This is because the signatures otherwise clashes with those from minecraft.
// The compiler accepts this, but complains, so we make it happy by prepending 'i'.
var Camera.ipos: Vec3d
    get() = pos
    set(value) = (this as ICamera).callSetPos(value)

fun Camera.setRotation(yaw: Float, pitch: Float) = (this as ICamera).callSetRotation(yaw, pitch)

fun Input.update(from: Input) = (this as ExtendedInput).update(from)

fun GameRenderer.setRenderHand(renderHand: Boolean) = (this as IGameRenderer).setRenderHand(renderHand)



var MinecraftClient.itemUseCooldown: Int
    get() = (this as IMinecraftClient).itemUseCooldown
    set(value) = (this as IMinecraftClient).setItemUseCooldown(value)

val Matrix4f.a00
    get() = (this as IMatrix4f).a00
val Matrix4f.a01
    get() = (this as IMatrix4f).a01
val Matrix4f.a02
    get() = (this as IMatrix4f).a02
val Matrix4f.a03
    get() = (this as IMatrix4f).a03
val Matrix4f.a10
    get() = (this as IMatrix4f).a10
val Matrix4f.a11
    get() = (this as IMatrix4f).a11
val Matrix4f.a12
    get() = (this as IMatrix4f).a12
val Matrix4f.a13
    get() = (this as IMatrix4f).a13
val Matrix4f.a20
    get() = (this as IMatrix4f).a20
val Matrix4f.a21
    get() = (this as IMatrix4f).a21
val Matrix4f.a22
    get() = (this as IMatrix4f).a22
val Matrix4f.a23
    get() = (this as IMatrix4f).a23
val Matrix4f.a30
    get() = (this as IMatrix4f).a30
val Matrix4f.a31
    get() = (this as IMatrix4f).a31
val Matrix4f.a32
    get() = (this as IMatrix4f).a32
val Matrix4f.a33
    get() = (this as IMatrix4f).a33
