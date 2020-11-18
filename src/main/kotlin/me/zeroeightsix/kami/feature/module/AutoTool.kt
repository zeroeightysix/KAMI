package me.zeroeightsix.kami.feature.module

import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting
import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.PlayerAttackBlockEvent
import me.zeroeightsix.kami.event.PlayerAttackEntityEvent
import me.zeroeightsix.kami.mixin.client.IClientPlayerInteractionManager
import me.zeroeightsix.kami.util.Texts
import net.minecraft.block.BlockState
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.enchantment.Enchantments
import net.minecraft.entity.EntityGroup
import net.minecraft.item.AxeItem
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.MiningToolItem
import net.minecraft.item.SwordItem
import net.minecraft.util.Formatting
import kotlin.math.pow

@Module.Info(
    name = "AutoTool",
    description = "Automatically switch to the best tools when mining or attacking",
    category = Module.Category.MISC
)
object AutoTool : Module() {
    @Setting
    var preferredWeapon = PreferredWeapon.AXE
    @Setting
    var swapEnabled = false
    @Setting
    var alertEnabled = false
    @Setting
    var swapDurability: @Setting.Constrain.Range(
    min = 0.0,
    max = 20.0, /* TODO: Remove when kotlin bug fixed */
    step = java.lang.Double.MIN_VALUE
    ) Int = 2
    @Setting
    var alertDurability: @Setting.Constrain.Range(
    min = 0.0,
    max = 20.0, /* TODO: Remove when kotlin bug fixed */
    step = java.lang.Double.MIN_VALUE
    ) Int = 5
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

    fun equipBestTool(blockState: BlockState) {
        if (alertEnabled&&(mc.player!!.mainHandStack.maxDamage - mc.player!!.mainHandStack.damage < alertDurability)) {
        mc.player!!.sendMessage(
            Texts.f(
                Formatting.DARK_RED, Texts.append(
                    Texts.lit("[ALERT]"),
                    Texts.flit(Formatting.WHITE, "Tool durability is Low")

                )
            ),
            true
        )}

        var bestSlot = -1
        var max = 0.0
        for (i in 0..8) {
            val stack = mc.player?.inventory?.getStack(i)
            if (stack != null) {
                if (stack.isEmpty || (swapEnabled&&(stack.maxDamage - stack.damage < swapDurability))) continue
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
        if (alertEnabled&&(mc.player!!.mainHandStack.maxDamage - mc.player!!.mainHandStack.damage < alertDurability)) {
            mc.player!!.sendMessage(
                Texts.f(
                    Formatting.DARK_RED, Texts.append(
                        Texts.lit("[ALERT]"),
                        Texts.flit(Formatting.WHITE, "Weapon durability is Low")

                    )
                ),
                true
            )}
        var bestSlot = -1
        var maxDamage = 0.0
        for (i in 0..8) {
            val stack = mc.player?.inventory?.getStack(i)
            if (stack != null) {
                if (stack.isEmpty || (swapEnabled&&(stack.maxDamage - stack.damage < swapDurability))) continue
            }
            if (stack != null) {
                val isPreferredWeapon = preferredWeapon.item.isAssignableFrom(stack.item::class.java)
                if ((stack.item is MiningToolItem && PreferredWeapon.values().none { it.item.isAssignableFrom(stack.item::class.java) }) || isPreferredWeapon) {
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

    private fun equip(slot: Int) {
        mc.player?.inventory?.selectedSlot = slot
        (mc.interactionManager as IClientPlayerInteractionManager).invokeSyncSelectedSlot()
    }

    enum class PreferredWeapon(val item: Class<out Item>, val damage: (ItemStack) -> Float?) {
        SWORD(SwordItem::class.java, { (it.item as? SwordItem)?.attackDamage }), AXE(AxeItem::class.java, { (it.item as? AxeItem)?.attackDamage })
    }
}
