package me.zeroeightsix.kami.feature.module

import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting
import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.TickEvent
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen
import net.minecraft.client.network.ClientPlayNetworkHandler
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.entity.player.PlayerInventory.isValidHotbarIndex
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.network.packet.c2s.play.PickFromInventoryC2SPacket
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction

@Module.Info(name = "AutoTotem", category = Module.Category.COMBAT)
object AutoTotem : Module() {
    @Setting(comment = "When enabled, AutoTotem will not replace your offhand if it's already carrying an item.")
    private var soft = false

    private val ItemStack.isTotem: Boolean
        get() = this.item === Items.TOTEM_OF_UNDYING

    @EventHandler
    val updateListener = Listener<TickEvent.Client.InGame>({
        if (mc.currentScreen is GenericContainerScreen) return@Listener
        val player = mc.player ?: return@Listener

        val offHand = player.offHandStack
        // The player already has a totem in their offhand, so there is no reason to look for one.
        // Or, if soft mode is on, and the player has a stack in their offhand, quit.
        // Or, if the player is moving something around in their inventory, don't intervene.
        if (offHand.isTotem ||
            (!offHand.isEmpty && soft) ||
            !player.inventory.cursorStack.isEmpty
        ) return@Listener

        player.inventory.main.forEach {
            if (it.isTotem) {
                val slot = player.inventory.getSlotWithStack(it)
                if (slot != -1) {
                    mc.networkHandler?.let { net ->
                        // Swap totem server-side.
                        // This works by:
                        // If the totem in in the hotbar, sending a packet selecting the slot the totem is in.
                        // Or, if the totem was not in the hotbar, sending a block pick packet to swap the totem slot with the currently selected slot.
                        // Then, send a swap offhand and main hand packet.
                        // Lastly, revert swapped slots and revert to the last selected slot.
                        swapServerSide(player.inventory, slot, net)

                        // Awesome: everything is swapped server side, but it might actually take a while before the client knows!
                        // The server has to respond, and tell us that our inventory has changed.
                        // In this time, AutoTotem might've already ran on the next tick, spamming these packets over and over.
                        // To fix this, we also instantly modify it client-sided, and hope to God there are no desyncs.
                        swapClientSide(player.inventory, slot)

                        // No need to swap any more totems: we're done for the day
                        return@Listener
                    }
                }
            }
        }
    })

    private fun swapServerSide(
        inventory: PlayerInventory,
        slot: Int,
        net: ClientPlayNetworkHandler
    ) {
        val selectedSlot = inventory.selectedSlot
        val alreadySelected = selectedSlot == slot
        val slotIsInHotbar = isValidHotbarIndex(slot)

        if (!slotIsInHotbar) {
            // 'Pick block' the slot we want to retrieve.
            // It will go to the currently selected slot.
            net.sendPacket(PickFromInventoryC2SPacket(slot))
        } else if (!alreadySelected) {
            // Instantly change the selected slot to the one with the totem in it
            net.sendPacket(UpdateSelectedSlotC2SPacket(slot))
        }

        // We now have a totem selected, so swap it with the offhand.
        net.sendPacket(
            PlayerActionC2SPacket(
                PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND,
                BlockPos.ORIGIN,
                Direction.DOWN
            )
        )

        if (!slotIsInHotbar) {
            // Swap the leftover back to its original slot
            net.sendPacket(PickFromInventoryC2SPacket(slot))
        }

        if (!alreadySelected) {
            // Move back to the real selected slot
            net.sendPacket(UpdateSelectedSlotC2SPacket(selectedSlot))
        }
    }

    fun swapClientSide(inventory: PlayerInventory, fromSlot: Int) {
        val offHandStack = inventory.offHand[0]
        inventory.offHand[0] = inventory.main[fromSlot]
        inventory.main[fromSlot] = offHandStack
    }

}
