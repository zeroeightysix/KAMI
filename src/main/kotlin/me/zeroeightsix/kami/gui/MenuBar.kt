package me.zeroeightsix.kami.gui

import imgui.dsl.mainMenuBar
import me.zeroeightsix.kami.gui.widgets.EnabledWidgets

object MenuBar {

    operator fun invoke() = mainMenuBar {
        EnabledWidgets()
    }

}