package me.zeroeightsix.kami.gui

import me.zeroeightsix.kami.gui.widgets.EnabledWidgets
import me.zeroeightsix.kami.gui.widgets.PinnableWidget
import me.zeroeightsix.kami.mc
import me.zeroeightsix.kami.tempSet
import net.minecraft.client.util.math.MatrixStack

object KamiHud {

    fun renderHud(matrixStack: MatrixStack) {
        if (mc.options.hudHidden) return
        KamiImgui.frame(matrixStack) {
            if (!EnabledWidgets.hideAll) {
                PinnableWidget.Companion::drawFadedBackground.tempSet(false) {
                    EnabledWidgets.widgets.removeAll { widget ->
                        widget.open && widget.pinned && widget.showWindow(false)
                    }
                }
            }
        }
    }

    fun getScale() = mc.window.scaleFactor.toFloat()
}
