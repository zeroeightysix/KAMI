package me.zeroeightsix.kami.gui.windows.modules

import glm_.vec4.Vec4
import imgui.Col
import imgui.ImGui.popStyleColor
import imgui.ImGui.pushStyleColor
import imgui.ImGui.sameLine
import imgui.ImGui.textWrapped
import imgui.api.demoDebugInformations.Companion.helpMarker
import io.github.fablabsmc.fablabs.api.fiber.v1.tree.ConfigLeaf
import me.zeroeightsix.kami.feature.command.getInterface
import me.zeroeightsix.kami.gui.windows.GraphicalSettings
import me.zeroeightsix.kami.feature.module.Module
import me.zeroeightsix.kami.flattenedStream

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

        module.config.flattenedStream().forEach {
            it.displayImGui()
        }
    }

}

// We introduce a generic <T> to allow displayImGui to be called even though we don't actually know the type of ConfigLeaf we have!
// the flattenedStream() method returns a stream over ConfigLeaf<*>, where <*> is any type.
// Because the interface (SettingInterface<T>) requires a ConfigLeaf<T> and we only have a ConfigLeaf<*> and SettingInterface<*> (and <*> != <*>), calling displayImGui is illegal.
// Instead, we can just avoid this by introducing a generic method that 'proves' (by implicit casting) that our type of ConfigLeaf is the same as its interface.
// Can't really do this inline (or I don't know how to), so I made a method to do it instead.
private fun <T> ConfigLeaf<T>.displayImGui() {
    this.getInterface().displayImGui(this)
}