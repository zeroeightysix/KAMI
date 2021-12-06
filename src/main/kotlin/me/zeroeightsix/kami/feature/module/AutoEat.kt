package me.zeroeightsix.kami.feature.module

import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting
import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.TickEvent.InGame
import me.zeroeightsix.kami.mixin.client.IKeyBinding
import me.zeroeightsix.kami.setting.SettingVisibility
import net.minecraft.client.option.KeyBinding
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemStack
import net.minecraft.util.Hand
import kotlin.math.roundToInt

@Module.Info(name = "AutoEat", description = "Automatically eat when hungry", category = Module.Category.PLAYER)
object AutoEat : Module() {
    private var eating: Hand? = null
    private var oldSlot: Int? = null

    @Setting(comment = "Use the threshold value to activate AutoEat instead of the food hunger value")
    private var useCustomThreshold = false

    @Setting(comment = "If the amount of free hunger points goes above this, AutoEat activates")
    @SettingVisibility.Method("shouldUseCustomThreshold")
    // there is absolutely no reason for this to be a double but i can't use an Int
    private var threshold: @Setting.Constrain.Range(min = 0.0, max = 20.0, step = 1.0) Int = 5

    @Setting
    private var priority = Priority.HUNGER_RESTORED

    // this is required for the settings as otherwise it will try to read before it's been initialized
    @Suppress("UNUSED")
    fun shouldUseCustomThreshold() = useCustomThreshold

    private fun isValid(stack: ItemStack, food: Int): Boolean {
        val restored = if (useCustomThreshold)
            threshold
        else
            stack.item?.foodComponent?.hunger ?: 0

        return stack.item.group === ItemGroup.FOOD && 20 - food >= restored
    }

    @EventHandler
    private val updateListener = Listener<InGame>({ it ->
        val player = it.player
        val foodLevel = player.hungerManager.foodLevel

        eating?.let { hand ->
            // Set the use keybinding to true. This is so minecraft doesn't try to cancel the eating action because the key is 'no longer' held down.
            KeyBinding.setKeyPressed((mc.options.keyUse as IKeyBinding).boundKey, true)
            mc.interactionManager?.interactItem(player, mc.world, hand)
            // If the current item isn't a valid food item, quit.
            // Usually happens when it is consumed.
            if (!isValid(player.inventory.getStack(player.inventory.selectedSlot), foodLevel)) {
                // Stop trying to eat from this hand
                eating = null
                // Revert the key use binding to false.
                KeyBinding.setKeyPressed((mc.options.keyUse as IKeyBinding).boundKey, false)
                oldSlot?.let {
                    // If we had an oldSlot (nonnull if AutoEat modified the selected slot), revert to it
                    player.inventory.selectedSlot = it
                    oldSlot = null
                }
            }
            return@Listener
        }
        if (player.isUsingItem) return@Listener

        if (isValid(player.offHandStack, foodLevel)) eating = Hand.OFF_HAND
        else {
            // No food in offhand, let's search the hotbar
            when (priority) {
                Priority.HOTBAR_POSITION -> {
                    (0..9).forEach { slot ->
                        if (isValid(player.inventory.getStack(slot), foodLevel)) {
                            eating = Hand.MAIN_HAND
                            oldSlot = player.inventory.selectedSlot
                            player.inventory.selectedSlot = slot
                            return@Listener
                        }
                    }
                }

                Priority.HUNGER_RESTORED -> {
                    (0..9).filter {
                        isValid(player.inventory.getStack(it), foodLevel)
                    }.maxBy {
                        (player.inventory.getStack(it).item.foodComponent?.hunger ?: 0)
                    }?.let {
                        eating = Hand.MAIN_HAND
                        oldSlot = player.inventory.selectedSlot
                        player.inventory.selectedSlot = it
                    }
                }
            }

        }
    })

    enum class Priority {
        HOTBAR_POSITION,
        HUNGER_RESTORED;
    }
}
