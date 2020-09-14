package me.zeroeightsix.kami.feature.module

import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting
import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting.Constrain
import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.TickEvent
import me.zeroeightsix.kami.util.Texts
import net.minecraft.item.Items
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

    @Setting(name = "CooldownOnOutput") // This is how often it will tell the player in chat
    private var updateEvery: @Constrain.Range(min = 2.0, max = 16.0, step = Double.MIN_VALUE) Int = 16

    private var lastUpdate: Long = 0L

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

                //Make sure the player is not being told that they themself are murderer
                if (username == mc.player?.displayName?.string) return@forEach

                //Check for weaponly items (swords, axes)
                player.itemsEquipped.forEach { it ->
                    if (weaponItems) when (it.item) {
                        Items.NETHERITE_SWORD -> mc.player?.sendMessage(murdererIs(username, it.item.name.string), false)
                        Items.DIAMOND_SWORD -> mc.player?.sendMessage(murdererIs(username, it.item.name.string), false)
                        Items.IRON_SWORD -> mc.player?.sendMessage(murdererIs(username, it.item.name.string), false)
                        Items.GOLDEN_SWORD -> mc.player?.sendMessage(murdererIs(username, it.item.name.string), false)
                        Items.WOODEN_SWORD -> mc.player?.sendMessage(murdererIs(username, it.item.name.string), false)

                        Items.NETHERITE_AXE -> mc.player?.sendMessage(murdererIs(username, it.item.name.string), false)
                        Items.DIAMOND_AXE -> mc.player?.sendMessage(murdererIs(username, it.item.name.string), false)
                        Items.IRON_AXE -> mc.player?.sendMessage(murdererIs(username, it.item.name.string), false)
                        Items.GOLDEN_AXE -> mc.player?.sendMessage(murdererIs(username, it.item.name.string), false)
                        Items.WOODEN_AXE -> mc.player?.sendMessage(murdererIs(username, it.item.name.string), false)
                    }

                    //Check for tool items like shovels, hoes, and shears, and picks (hypixel knife skin)
                    if (toolItems) when (it.item) {
                        Items.NETHERITE_SHOVEL -> mc.player?.sendMessage(murdererIs(username, it.item.name.string), false)
                        Items.DIAMOND_SHOVEL -> mc.player?.sendMessage(murdererIs(username, it.item.name.string), false)
                        Items.IRON_SHOVEL -> mc.player?.sendMessage(murdererIs(username, it.item.name.string), false)
                        Items.GOLDEN_SHOVEL -> mc.player?.sendMessage(murdererIs(username, it.item.name.string), false)
                        Items.WOODEN_SHOVEL -> mc.player?.sendMessage(murdererIs(username, it.item.name.string), false)

                        Items.NETHERITE_HOE -> mc.player?.sendMessage(murdererIs(username, it.item.name.string), false)
                        Items.DIAMOND_HOE -> mc.player?.sendMessage(murdererIs(username, it.item.name.string), false)
                        Items.IRON_HOE -> mc.player?.sendMessage(murdererIs(username, it.item.name.string), false)
                        Items.GOLDEN_HOE -> mc.player?.sendMessage(murdererIs(username, it.item.name.string), false)
                        Items.WOODEN_HOE -> mc.player?.sendMessage(murdererIs(username, it.item.name.string), false)

                        Items.NETHERITE_PICKAXE -> mc.player?.sendMessage(murdererIs(username, it.item.name.string), false)
                        Items.DIAMOND_PICKAXE -> mc.player?.sendMessage(murdererIs(username, it.item.name.string), false)
                        Items.IRON_PICKAXE -> mc.player?.sendMessage(murdererIs(username, it.item.name.string), false)
                        Items.GOLDEN_PICKAXE -> mc.player?.sendMessage(murdererIs(username, it.item.name.string), false)
                        Items.WOODEN_PICKAXE -> mc.player?.sendMessage(murdererIs(username, it.item.name.string), false)

                        Items.SHEARS -> mc.player?.sendMessage(murdererIs(username, it.item.name.string), false)
                    }

                    //Check for otherwise innocent items that could be knife skins on some servers
                    if (innocuousItems) when (it.item) {
                        Items.CARROT -> mc.player?.sendMessage(murdererIs(username, it.item.name.string), false)
                        Items.BONE -> mc.player?.sendMessage(murdererIs(username, it.item.name.string), false)
                        Items.CARROT_ON_A_STICK -> mc.player?.sendMessage(murdererIs(username, it.item.name.string), false)
                        Items.GOLDEN_CARROT -> mc.player?.sendMessage(murdererIs(username, it.item.name.string), false)
                        Items.BLAZE_ROD -> mc.player?.sendMessage(murdererIs(username, it.item.name.string), false)
                        Items.SPONGE -> mc.player?.sendMessage(murdererIs(username, it.item.name.string), false)
                    }

                    //Check for items the detective or innocent players could use to stop them murderer
                    if (detectiveItems) when (it.item) {
                        Items.BOW -> mc.player?.sendMessage(detectiveIs(username, it.item.name.string), false)
                        Items.SNOWBALL -> mc.player?.sendMessage(detectiveIs(username, it.item.name.string), false)
                        Items.ARROW -> mc.player?.sendMessage(detectiveIs(username, it.item.name.string), false)
                    }
                }

            }
    })
}
