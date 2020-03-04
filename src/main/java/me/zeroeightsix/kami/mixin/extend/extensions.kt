package me.zeroeightsix.kami.mixin.extend

import me.zeroeightsix.kami.mixin.client.IEntityRenderDispatcher
import me.zeroeightsix.kami.mixin.client.IGameRenderer
import net.minecraft.client.render.GameRenderer
import net.minecraft.client.render.entity.EntityRenderDispatcher

fun EntityRenderDispatcher.getRenderPosX() = (this as IEntityRenderDispatcher).renderPosX
fun EntityRenderDispatcher.getRenderPosY() = (this as IEntityRenderDispatcher).renderPosY
fun EntityRenderDispatcher.getRenderPosZ() = (this as IEntityRenderDispatcher).renderPosZ

fun GameRenderer.applyCameraTransformations(tickDelta: Float) = (this as IGameRenderer).invokeApplyCameraTransformations(tickDelta)