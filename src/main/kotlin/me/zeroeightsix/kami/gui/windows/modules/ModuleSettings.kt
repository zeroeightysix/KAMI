package me.zeroeightsix.kami.gui.windows.modules

import imgui.ImGui
import imgui.api.demoDebugInformations
import me.zeroeightsix.kami.module.Module

object ModuleSettings {

    operator fun invoke(module: Module, source: Modules.ModuleWindow) {
        with (ImGui) {
            val moduleName = module.name
            text("These are the settings for $moduleName")
            sameLine()
            demoDebugInformations.helpMarker(module.description)
        }
    }

}