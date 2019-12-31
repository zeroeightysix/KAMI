package me.zeroeightsix.kami.gui.widgets

import imgui.ImGui.text
import me.zeroeightsix.kami.module.Module

private typealias TreeNodeFlags = Int

object ModuleSettings {

    operator fun invoke(module: Module) {
        text("These are the settings for ${module.name}.")
    }

}