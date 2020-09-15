package me.zeroeightsix.kami.feature.module

import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting
import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting.Constrain
import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.TickEvent
import me.zeroeightsix.kami.util.Texts
import net.minecraft.item.*
import net.minecraft.text.MutableText
import net.minecraft.util.Formatting

@Module.Info(
        name = "MurderMysteries",
        category = Module.Category.MISC,
        description = "Get notified when a player holds a dangerous item"
)
object MurderMysteries : Module() {

    @Setting(name = "InnocuousItems")
    private var innocuousItems = true
    @Setting(name = "WeaponItems")
    private var weaponItems = true
    @Setting(name = "ToolItems")
    private var toolItems = true
    @Setting(name = "DetectiveItems")
    private var detectiveItems = false
    @Setting(name = "IgnoreWoodenShovel")
    private var ignoreWoodenShovel = false

    @Setting(name = "CooldownOnOutput") // This is how often it will tell the player in chat
    private var updateEvery: @Constrain.Range(min = 2.0, max = 16.0, step = 0.1) Int = 10

    private var lastUpdate: Long = 0L

    //I know its deprecated but I am not sure how I should go about this.
    private fun murdererIs(username: String, item: String): MutableText? {
        lastUpdate = System.currentTimeMillis()
        return Texts.f(Formatting.WHITE, Texts.append(
                Texts.lit("[MurderMysteries] "),
                Texts.flit(Formatting.AQUA, username),
                Texts.flit(Formatting.WHITE, " is the "),
                Texts.flit(Formatting.RED, "murderer! "),
                Texts.flit(Formatting.WHITE, "Wields "),
                Texts.flit(Formatting.GOLD, "$item!")
        ))
    }
    private fun detectiveIs(username: String, item: String): MutableText? {
        lastUpdate = System.currentTimeMillis()
        return Texts.f(Formatting.WHITE, Texts.append(
                Texts.lit("[MurderMysteries] "),
                Texts.flit(Formatting.AQUA, username),
                Texts.flit(Formatting.WHITE, " has "),
                Texts.flit(Formatting.GOLD, "$item!")
        ))
    }

    @EventHandler
    val worldListener = Listener<TickEvent.Client.InGame>({
        if ((lastUpdate + updateEvery * 1000L <= System.currentTimeMillis()))
            mc.world?.players?.forEach { player ->
                val username = player.displayName.string

                //Make sure the player is not being told that they are the murderer
                if (player == mc.player) return@forEach

                val innocentItemsList = listOf(
                    Items.CARROT,
                    Items.BONE,
                    Items.CARROT_ON_A_STICK,
                    Items.GOLDEN_CARROT,
                    Items.BLAZE_ROD,
                    Items.SPONGE
                )
                val detectiveItemsList = listOf(
                    Items.BOW,
                    Items.SNOWBALL,
                    Items.ARROW
                )
                //Check for weaponly items (swords, axes)s
                player.itemsEquipped.forEach { it ->
                    if (weaponItems && (it.item is AxeItem || it.item is SwordItem))
                        mc.player?.sendMessage(murdererIs(username, it.item.name.string), false)

                    //Check for tool items like shovels, hoes, and shears, and picks (hypixel knife skin)
                    if (toolItems && (it.item is ShovelItem || it.item is PickaxeItem || it.item is HoeItem || it.item is ShearsItem) && !(it.item == Items.WOODEN_SHOVEL && ignoreWoodenShovel))
                        mc.player?.sendMessage(murdererIs(username, it.item.name.string), false)

                    //Check for otherwise innocent items that could be knife skins on some servers
                    if (innocuousItems && (it.item in innocentItemsList))
                        mc.player?.sendMessage(murdererIs(username, it.item.name.string), false)

                    //Check for items the detective or innocent players could use to stop them murderer
                    if (detectiveItems && it.item in detectiveItemsList)
                        mc.player?.sendMessage(detectiveIs(username, it.item.name.string), false)

                }
            }
    })
}
