package me.zeroeightsix.kami.feature.module

import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting
import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting.Constrain
import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.TickEvent
import me.zeroeightsix.kami.util.text
import net.minecraft.item.AxeItem
import net.minecraft.item.HoeItem
import net.minecraft.item.Items
import net.minecraft.item.PickaxeItem
import net.minecraft.item.ShearsItem
import net.minecraft.item.ShovelItem
import net.minecraft.item.SwordItem
import net.minecraft.nbt.Tag.AQUA
import net.minecraft.nbt.Tag.GOLD
import net.minecraft.nbt.Tag.RED

@Module.Info(
    name = "MurderMysteries",
    category = Module.Category.MISC,
    description = "Get notified when a player holds a dangerous item"
)
object MurderMysteries : Module() {

    @Setting
    private var innocuousItems = true

    @Setting
    private var weaponItems = true

    @Setting
    private var toolItems = true

    @Setting
    private var detectiveItems = false

    @Setting(comment = "Ignore wooden shovels from ToolItems")
    private var ignoreWoodenShovel = false

    @Setting(
        name = "CooldownOnOutput",
        comment = "How often it will say who is murderer"
    ) // This is how often it will tell the player in chat
    private var updateEvery: @Constrain.Range(min = 3.0, max = 16.0, step = 0.5) Float = 8f

    @Setting(comment = "Say who the murderer is in chat (WARNING: VERY EVIDENT YOU ARE USING THIS MODULE)")
    private var announceMode = false

    private var lastUpdate: Long = 0L

    private val innocentItemsList = listOf(
        Items.CARROT,
        Items.BONE,
        Items.CARROT_ON_A_STICK,
        Items.GOLDEN_CARROT,
        Items.BLAZE_ROD,
        Items.SPONGE,
        Items.COBWEB,
        Items.FEATHER,
        Items.STICK,
        Items.SALMON,
        Items.COOKED_SALMON,
        Items.COD,
        Items.COOKED_COD,
        Items.TROPICAL_FISH
    )
    private val detectiveItemsList = listOf(
        Items.BOW,
        Items.SNOWBALL,
        Items.ARROW
    )

    @EventHandler
    val worldListener = Listener<TickEvent.InGame>({
        val player = it.player
        if ((lastUpdate + (updateEvery * 1000f).toLong() <= System.currentTimeMillis()))
            mc.world?.players?.forEach { otherPlayer ->
                // Make sure the player is not being told that they are the murderer
                if (otherPlayer == player) return@forEach

                // Check for weaponly items (swords, axes)s
                otherPlayer.itemsEquipped.forEach { itemStack ->
                    if ((weaponItems && (itemStack.item is AxeItem || itemStack.item is SwordItem)) ||
                        (innocuousItems && (itemStack.item in innocentItemsList)) ||
                        (
                            toolItems && (
                                itemStack.item is ShovelItem || itemStack.item is PickaxeItem ||
                                    itemStack.item is HoeItem || itemStack.item is ShearsItem
                                ) &&
                                !(itemStack.item == Items.WOODEN_SHOVEL && ignoreWoodenShovel)
                            )
                    ) {
                        lastUpdate = System.currentTimeMillis()
                        player.sendMessage(
                            text {
                                +"[MurderMysteries] "
                                +otherPlayer.displayName.string(AQUA)
                                +" is the "
                                +"murderer"(RED)
                                +"! Wields "
                                +itemStack.item.name.string(GOLD)
                                +"!"
                            },
                            false
                        )
                        if (announceMode) player.sendChatMessage("${otherPlayer.displayName.string} is the murderer! They have  ${itemStack.item.name.string}!")
                    }

                    // Check for items the detective or innocent players could use to stop them murderer
                    if (detectiveItems && itemStack.item in detectiveItemsList) {
                        lastUpdate = System.currentTimeMillis()
                        player.sendMessage(
                            text {
                                +"[MurderMysteries] "
                                +otherPlayer.displayName.string(AQUA)
                                +" has "
                                +itemStack.item.name.string(GOLD)
                                +"!"
                            },
                            false
                        )
                        if (announceMode) player.sendChatMessage("${otherPlayer.displayName.string} has ${itemStack.item.name.string}!")
                    }
                }
            }
    })
}
