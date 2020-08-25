package me.zeroeightsix.kami.feature.module

import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting
import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.TickEvent
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.screen.slot.SlotActionType

@Module.Info(name = "AutoTotem", category = Module.Category.COMBAT)
object AutoTotem : Module() {
    var totems = 0
    var moving = false
    var returnI = false

    @Setting(comment = "When enabled, AutoTotem will not replace your offhand if it's already carrying an item.")
    private var soft = false

    @EventHandler
    val updateListener = Listener<TickEvent.Client.InGame>({
        if (mc.currentScreen is GenericContainerScreen) return@Listener
        if (returnI) {
            moveTotem()
        }
        totems = mc.player!!.inventory.main.stream()
            .filter { itemStack -> itemStack.item === Items.TOTEM_OF_UNDYING }.mapToInt(ItemStack::getCount).sum()
        if (mc.player?.offHandStack?.item === Items.TOTEM_OF_UNDYING) totems++ else {
            if (soft && mc.player?.offHandStack?.isEmpty != true) return@Listener
            if (moving) {
                mc.interactionManager?.clickSlot(0, 45, 0, SlotActionType.PICKUP, mc.player)
                moving = false
                if (!mc.player!!.inventory.cursorStack.isEmpty) returnI = true
                return@Listener
            }
            if (mc.player!!.inventory.cursorStack.isEmpty) {
                if (totems == 0) return@Listener
                var t = -1
                for (i in 0..44) if (mc.player!!.inventory.getStack(i).item === Items.TOTEM_OF_UNDYING) {
                    t = i
                    break
                }
                if (t == -1) return@Listener // Should never happen!
                mc.interactionManager?.clickSlot(0, if (t < 9) t + 36 else t, 0, SlotActionType.PICKUP, mc.player)
                moving = true
            } else if (!soft) {
                if (moveTotem()) return@Listener
            }
        }
    })

    private fun moveTotem(): Boolean {
        var t = -1
        for (i in 0..44) if (mc.player!!.inventory.getStack(i).isEmpty) {
            t = i
            break
        }
        if (t == -1) return true
        mc.interactionManager?.clickSlot(0, if (t < 9) t + 36 else t, 0, SlotActionType.PICKUP, mc.player)
        return false
    }
}
