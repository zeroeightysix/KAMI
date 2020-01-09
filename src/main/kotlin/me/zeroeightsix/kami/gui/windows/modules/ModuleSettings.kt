package me.zeroeightsix.kami.gui.windows.modules

import glm_.vec4.Vec4
import imgui.Col
import imgui.ImGui.popStyleColor
import imgui.ImGui.pushStyleColor
import imgui.ImGui.sameLine
import imgui.ImGui.textWrapped
import imgui.api.demoDebugInformations.Companion.helpMarker
import me.zeroeightsix.kami.gui.windows.KamiSettings
import me.zeroeightsix.kami.module.Module

object ModuleSettings {

    operator fun invoke(module: Module, block: () -> Unit) {
        val markerShown = !KamiSettings.hideModuleMarker
        if (!KamiSettings.hideModuleDescriptions) {
            pushStyleColor(Col.Text, Vec4(.7f, .7f, .7f, 1f))
            textWrapped(module.description)
            popStyleColor()
            if (markerShown)
                sameLine()
        }
        if (markerShown) {
            helpMarker("Start dragging from this question mark to merge this module into another module window. Right click this question mark and press 'Detach' to seperate it into a new window.")
        }
        block()

        module.settingList.filter{ it.isVisible }.forEach {
            it.drawSettings()
        }
    }

}