package me.zeroeightsix.kami.feature.module

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.PlayerAttackBlockEvent
import me.zeroeightsix.kami.event.PlayerAttackEntityEvent
import me.zeroeightsix.kami.mixin.client.IClientPlayerInteractionManager
import net.minecraft.block.BlockState
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.enchantment.Enchantments
import net.minecraft.entity.EntityGroup
import net.minecraft.item.MiningToolItem
import net.minecraft.item.SwordItem
import kotlin.math.pow

@Module.Info(
    name = "AutoTool",
    description = "Automatically switch to the best tools when mining or attacking",
    category = Module.Category.MISC
)
object AutoTool : Module() {
    @EventHandler
    private val leftClickListener =
        Listener(
            { event: PlayerAttackBlockEvent ->
                mc.world?.getBlockState(
                    event.position
                )?.let {
                    equipBestTool(
                        it
                    )
                }
            }
        )

    @EventHandler
    private val attackListener =
        Listener(
            EventHook<PlayerAttackEntityEvent> { event: PlayerAttackEntityEvent? -> equipBestWeapon() }
        )

    private fun equipBestTool(blockState: BlockState) {
        var bestSlot = -1
        var max = 0.0
        for (i in 0..8) {
            val stack = mc.player?.inventory?.getStack(i)
            if (stack != null) {
                if (stack.isEmpty) continue
            }
            var speed = stack?.getMiningSpeedMultiplier(blockState)
            var eff: Int
            if (speed != null) {
                if (speed > 1) {
                    speed += (
                        if (EnchantmentHelper.getLevel(Enchantments.EFFICIENCY, stack).also {
                                eff = it
                            } > 0
                        ) (eff.toDouble().pow(2.0) + 1) else 0.0
                        ).toFloat()
                    if (speed > max) {
                        max = speed.toDouble()
                        bestSlot = i
                    }
                }
            }
        }
        if (bestSlot != -1) equip(bestSlot)
    }

    fun equipBestWeapon() {
        var bestSlot = -1
        var maxDamage = 0.0
        for (i in 0..8) {
            val stack = mc.player?.inventory?.getStack(i)
            if (stack != null) {
                if (stack.isEmpty) continue
            }
            if (stack != null) {
                if (stack.item is MiningToolItem || stack.item is SwordItem) {
                    // Not sure of the best way to cast stack.item as either SwordItem or MiningToolItem
                    val damage = if (stack.item is SwordItem) {
                        (stack.item as SwordItem).attackDamage + EnchantmentHelper.getAttackDamage(
                            stack,
                            EntityGroup.DEFAULT
                        ).toDouble()
                    } else {
                        (stack.item as MiningToolItem).attackDamage + EnchantmentHelper.getAttackDamage(
                            stack,
                            EntityGroup.DEFAULT
                        ).toDouble()
                    }
                    if (damage > maxDamage) {
                        maxDamage = damage
                        bestSlot = i
                    }
                }
            }
        }
        if (bestSlot != -1) equip(bestSlot)
    }

    private fun equip(slot: Int) {
        mc.player?.inventory?.selectedSlot = slot
        (mc.interactionManager as IClientPlayerInteractionManager).invokeSyncSelectedSlot()
    }
}
