package me.zeroeightsix.kami.feature.hidden

import me.zeroeightsix.kami.feature.FindFeature
import me.zeroeightsix.kami.feature.FullFeature
import me.zeroeightsix.kami.gui.KamiGuiScreen
import me.zeroeightsix.kami.util.Wrapper

@FindFeature
class ClickGui : FullFeature(originalName = "ClickGui", _alwaysListening = true) {

    override fun onEnable() {
        super.onEnable()
        Wrapper.getMinecraft().openScreen(KamiGuiScreen)
        disable()
    }

}
