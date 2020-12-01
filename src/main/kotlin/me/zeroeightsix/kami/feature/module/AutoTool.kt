package me.zeroeightsix.kami.feature.module

import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting
import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.PlayerAttackBlockEvent
import me.zeroeightsix.kami.event.PlayerAttackEntityEvent
import me.zeroeightsix.kami.mixin.client.IClientPlayerInteractionManager
import net.minecraft.block.BlockState
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.EntityGroup
import net.minecraft.item.AxeItem
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.MiningToolItem
import net.minecraft.item.SwordItem
import net.minecraft.util.math.BlockPos

@Module.Info(
    name = "AutoTool",
    description = "Automatically switch to the best tools when mining or attacking",
    category = Module.Category.MISC
)
object AutoTool : Module() {
    @Setting
    var preferredWeapon = PreferredWeapon.AXE

    @EventHandler
    private val leftClickListener =
        Listener(
            { event: PlayerAttackBlockEvent ->
                mc.world?.getBlockState(
                    event.position
                )?.let {
                    equipBestTool(
                        it,
                        event.position
                    )
                }
            }
        )

    @EventHandler
    private val attackListener =
        Listener(
            { _: PlayerAttackEntityEvent? -> equipBestWeapon() }
        )

    fun equipBestTool(blockState: BlockState, blockPos: BlockPos) {
        val previousSlot = mc.player?.inventory?.selectedSlot
        var bestSlot = -1
        var max = 0f
        for (i in 0..8) {
            val stack = mc.player?.inventory?.getStack(i)
            if (stack != null) {
                if (stack.isEmpty) continue
            }
            equip(i, updateServer = false)
            @Suppress("DEPRECATION")
            val delta = blockState.block.calcBlockBreakingDelta(blockState, mc.player, mc.world, blockPos)
            if (delta > max) {
                max = delta
                bestSlot = i
            }
        }
        if (bestSlot != -1)
            equip(bestSlot)
        else
            // if something went wrong, we will reset the selected slot to avoid desyncs
            previousSlot?.let { equip(it, updateServer = false) }
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
                val isPreferredWeapon = preferredWeapon.item.isAssignableFrom(stack.item::class.java)
                if ((stack.item is MiningToolItem && PreferredWeapon.values()
                        .none { it.item.isAssignableFrom(stack.item::class.java) }) || isPreferredWeapon
                ) {
                    val damage = if (isPreferredWeapon) {
                        preferredWeapon.damage(stack)!! + EnchantmentHelper.getAttackDamage(
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

    private fun equip(slot: Int, updateServer: Boolean = true) {
        mc.player?.inventory?.selectedSlot = slot
        if (updateServer)
            (mc.interactionManager as IClientPlayerInteractionManager).invokeSyncSelectedSlot()
    }

    enum class PreferredWeapon(val item: Class<out Item>, val damage: (ItemStack) -> Float?) {
        SWORD(SwordItem::class.java, { (it.item as? SwordItem)?.attackDamage }),
        AXE(AxeItem::class.java, { (it.item as? AxeItem)?.attackDamage })
    }
}
