package me.zeroeightsix.kami.gui.windows.modules

import glm_.vec4.Vec4
import imgui.Col
import imgui.ImGui.popStyleColor
import imgui.ImGui.pushStyleColor
import imgui.ImGui.sameLine
import imgui.ImGui.textWrapped
import imgui.api.demoDebugInformations.Companion.helpMarker
import me.zeroeightsix.kami.gui.windows.GraphicalSettings
import me.zeroeightsix.kami.feature.module.Module

object ModuleSettings {

    operator fun invoke(module: Module, block: () -> Unit) {
        val editMarkerShown = GraphicalSettings.oldModuleEditMode
        if (!GraphicalSettings.hideModuleDescriptions) {
            pushStyleColor(Col.Text, Vec4(.7f, .7f, .7f, 1f))
            textWrapped(module.description)
            popStyleColor()
            if (editMarkerShown)
                sameLine()
        }
        if (editMarkerShown) {
            helpMarker("Start dragging from this question mark to merge this module into another module window. Right click this question mark and press 'Detach' to seperate it into a new window.")
        }
        block()

        // TODO
//        module.settingList.filter{ it.isVisible }.forEach {
//            it.drawSettings()
//        }
    }

}