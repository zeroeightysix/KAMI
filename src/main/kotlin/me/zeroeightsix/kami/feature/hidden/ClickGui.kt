package me.zeroeightsix.kami.feature.hidden

import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting
import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listenable
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.event.BindEvent
import me.zeroeightsix.kami.expectingInput
import me.zeroeightsix.kami.feature.Feature
import me.zeroeightsix.kami.feature.FindFeature
import me.zeroeightsix.kami.feature.HasBind
import me.zeroeightsix.kami.gui.KamiGuiScreen
import me.zeroeightsix.kami.gui.KamiHud
import me.zeroeightsix.kami.gui.windows.Settings
import me.zeroeightsix.kami.mc
import me.zeroeightsix.kami.util.Bind
import net.minecraft.client.util.InputUtil

@FindFeature
object ClickGui : Feature, Listenable, HasBind {

    @Setting
    override var bind: Bind = Bind(false, false, false, Bind.Code(InputUtil.fromTranslationKey("key.keyboard.y")))

    @EventHandler
    val bindListener = Listener(
        EventHook<BindEvent> {
            bind.update(it.key, it.scancode, it.pressed)
            if (bind.isDown && (it.ingame || (Settings.openGuiAnywhere && mc.currentScreen?.expectingInput != true && mc.currentScreen !is KamiGuiScreen))) {
                KamiHud // In case imgui was not yet initialised, it gets initialised here. (Kotlin `init` block)
                mc.setScreen(KamiGuiScreen(mc.currentScreen))
                it.cancel()
            }
        }
    )

    override var name: String = "ClickGui"
    override var hidden: Boolean = false

    override fun init() {
        super.init()
        KamiMod.EVENT_BUS.subscribe(bindListener)
    }
}