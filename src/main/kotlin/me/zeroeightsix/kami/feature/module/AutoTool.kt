package me.zeroeightsix.kami.feature.module

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.events.PlayerAttackBlockEvent
import me.zeroeightsix.kami.event.events.PlayerAttackEntityEvent
import me.zeroeightsix.kami.mixin.client.IClientPlayerInteractionManager
import me.zeroeightsix.kami.mixin.client.IMiningToolItem
import net.minecraft.block.BlockState
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.enchantment.Enchantments
import net.minecraft.entity.EntityGroup
import net.minecraft.item.MiningToolItem
import net.minecraft.item.SwordItem
import kotlin.math.pow

/**
 * Created by 086 on 2/10/2018.
 */
@Module.Info(
    name = "AutoTool",
    description = "Automatically switch to the best tools when mining or attacking",
    category = Module.Category.MISC
)
object AutoTool : Module() {
    @EventHandler
    private val leftClickListener =
        Listener(
            EventHook { event: PlayerAttackBlockEvent ->
                equipBestTool(
                    mc.world.getBlockState(
                        event.position
                    )
                )
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
            val stack = mc.player.inventory.getInvStack(i)
            if (stack.isEmpty) continue
            var speed = stack.getMiningSpeed(blockState)
            var eff: Int
            if (speed > 1) {
                speed += (if (EnchantmentHelper.getLevel(Enchantments.EFFICIENCY, stack).also {
                        eff = it
                    } > 0) (eff.toDouble().pow(2.0) + 1) else 0.0).toFloat()
                if (speed > max) {
                    max = speed.toDouble()
                    bestSlot = i
                }
            }
        }
        if (bestSlot != -1) equip(bestSlot)
    }

    fun equipBestWeapon() {
        var bestSlot = -1
        var maxDamage = 0.0
        for (i in 0..8) {
            val stack = mc.player.inventory.getInvStack(i)
            if (stack.isEmpty) continue
            if (stack.item is MiningToolItem || stack.item is SwordItem) {
                val damage =
                    (stack.item as IMiningToolItem).attackDamage + EnchantmentHelper.getAttackDamage(
                        stack,
                        EntityGroup.DEFAULT
                    ).toDouble()
                if (damage > maxDamage) {
                    maxDamage = damage
                    bestSlot = i
                }
            }
        }
        if (bestSlot != -1) equip(bestSlot)
    }

    private fun equip(slot: Int) {
        mc.player.inventory.selectedSlot = slot
        (mc.interactionManager as IClientPlayerInteractionManager).invokeSyncSelectedSlot()
    }
}