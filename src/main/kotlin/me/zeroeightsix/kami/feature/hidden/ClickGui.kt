package me.zeroeightsix.kami.feature.hidden

import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting
import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listenable
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.event.BindEvent
import me.zeroeightsix.kami.feature.Feature
import me.zeroeightsix.kami.feature.FindFeature
import me.zeroeightsix.kami.gui.KamiGuiScreen
import me.zeroeightsix.kami.mc
import me.zeroeightsix.kami.util.Bind
import net.minecraft.client.util.InputUtil

@FindFeature
object ClickGui : Feature, Listenable {

    @Setting
    var bind: Bind = Bind(false, false, false, Bind.Code(InputUtil.fromTranslationKey("key.keyboard.y")))

    @EventHandler
    val bindListener = Listener(
        EventHook<BindEvent> {
            bind.update(it.key, it.scancode, it.pressed)
            if (bind.isDown && it.ingame) {
                mc.openScreen(KamiGuiScreen)
            }
        }
    )

    override var name: String = "ClickGui"
    override var hidden: Boolean = false

    override fun initListening() {
        KamiMod.EVENT_BUS.subscribe(bindListener)
    }

}
