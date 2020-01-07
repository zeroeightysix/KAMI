package me.zeroeightsix.kami.gui.windows.modules

import imgui.ImGui.sameLine
import imgui.ImGui.text
import imgui.api.demoDebugInformations.Companion.helpMarker
import me.zeroeightsix.kami.module.Module

object ModuleSettings {

    operator fun invoke(module: Module, block: () -> Unit) {
        text(module.description)
        sameLine()
        helpMarker("Start dragging from this question mark to merge this module into another module window. Right click this question mark and press 'Detach' to seperate it into a new window.")
        block()

        module.settingList.filter{ it.isVisible }.forEach {
            it.drawSettings()
        }
    }

}