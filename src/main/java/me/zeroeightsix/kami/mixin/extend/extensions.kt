package me.zeroeightsix.kami.mixin.extend

import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import me.zeroeightsix.kami.mixin.client.IGameRenderer
import me.zeroeightsix.kami.mixin.client.IInputUtilType
import net.minecraft.client.render.GameRenderer
import net.minecraft.client.util.InputUtil

fun GameRenderer.applyCameraTransformations(tickDelta: Float) = (this as IGameRenderer).invokeApplyCameraTransformations(tickDelta)

fun InputUtil.Type.getMap(): Int2ObjectMap<InputUtil.KeyCode> = (this as IInputUtilType).map
