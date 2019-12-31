package me.zeroeightsix.kami.gui.windows

import imgui.ImGui
import imgui.ImGui.currentWindow
import imgui.ImGui.isItemClicked
import imgui.ImGui.text
import imgui.ImGui.treeNodeBehaviorIsOpen
import imgui.ImGui.treeNodeExV
import imgui.ImGui.treePop
import imgui.MouseButton
import imgui.TreeNodeFlag
import imgui.dsl.collapsingHeader
import imgui.dsl.window
import imgui.internal.ItemStatusFlag
import imgui.internal.or
import imgui.or
import me.zeroeightsix.kami.gui.widgets.ModuleSettings
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.module.ModuleManager

object KamiModules {

    private val windows = mutableListOf<ModuleWindow>(MultiGroupWindow("All modules", ModuleManager.modules.groupBy { it.category.getName() }))
    val baseFlags = TreeNodeFlag.SpanFullWidth.i or TreeNodeFlag.OpenOnDoubleClick
    
    private fun collapsibleModule(module: Module) {
        val nodeFlags = if (!module.isEnabled) baseFlags else (baseFlags or TreeNodeFlag.Selected)
        val label = "${module.name}-node"

        // We don't want imgui to handle open/closing at all, so we hack out the behaviour
        val doubleClicked = ImGui.io.mouseDoubleClicked[0]
        ImGui.io.mouseDoubleClicked[0] = false

        var clickedLeft = false
        var clickedRight = false

        fun updateClicked() {
            clickedLeft = isItemClicked(MouseButton.Left)
            clickedRight = isItemClicked(MouseButton.Right)
        }

        if (treeNodeExV(label, nodeFlags, module.name)) {
            updateClicked()
            ModuleSettings(module)
            treePop()
        } else updateClicked()

        // Restore state
        ImGui.io.mouseDoubleClicked[0] = doubleClicked

        if (clickedLeft) {
            module.isEnabled = !module.isEnabled
        } else if (clickedRight) {
            val id = currentWindow.getId(label)
            val open = treeNodeBehaviorIsOpen(id, nodeFlags)
            val window = currentWindow
            window.dc.stateStorage[id] = !open
            window.dc.lastItemStatusFlags = window.dc.lastItemStatusFlags or ItemStatusFlag.ToggledOpen
        }
    }

    operator fun invoke() {
        windows.forEach {
            it.draw()
        }
    }

    abstract class ModuleWindow(val title: String) {

        fun draw() {
            window(title) {
                fill()
            }
        }

        protected abstract fun fill()

    }

    class OneModuleWindow(title: String, val module: Module) : ModuleWindow(title) {

        var moduleEnabled = module.isEnabled

        override fun fill() {
            if (ImGui.checkbox("Enabled", ::moduleEnabled)) {
                module.isEnabled = moduleEnabled
            }
            ModuleSettings(module)
        }

    }

    class MultiModuleWindow(title: String, val modules: List<Module>) : ModuleWindow(title) {

        override fun fill() {
            modules.forEach {
                collapsibleModule(it)
            }
        }

    }
    
    class MultiGroupWindow(title: String, val groups: Map<String, List<Module>>) : ModuleWindow(title) {

        override fun fill() {
            for ((group, list) in groups) {
                collapsingHeader(group) {
                    if (list.isEmpty()) {
                        text("Ain't nobody here but us chickens.")
                    } else {
                        list.forEach {
                            collapsibleModule(it)
                        }
                    }
                }
            }
        }

    }

}