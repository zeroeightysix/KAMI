package me.zeroeightsix.kami.gui.windows.modules

import glm_.vec2.Vec2
import imgui.*
import imgui.ImGui.acceptDragDropPayload
import imgui.ImGui.currentWindow
import imgui.ImGui.isItemClicked
import imgui.ImGui.setDragDropPayload
import imgui.ImGui.setNextWindowPos
import imgui.ImGui.text
import imgui.ImGui.treeNodeBehaviorIsOpen
import imgui.ImGui.treeNodeExV
import imgui.ImGui.treePop
import imgui.dsl.dragDropSource
import imgui.dsl.dragDropTarget
import imgui.dsl.menuItem
import imgui.dsl.popupContextItem
import imgui.dsl.window
import imgui.internal.ItemStatusFlag
import imgui.internal.or
import me.zeroeightsix.kami.gui.windows.KamiSettings
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.module.ModuleManager
import me.zeroeightsix.kami.gui.windows.modules.Payloads.KAMI_MODULE_PAYLOAD

object Modules {

    private val windows = mutableListOf(
        ModuleWindow("All modules", groups = ModuleManager.modules.groupBy {
            it.category.getName()
        }.mapValuesTo(mutableMapOf(), { entry -> entry.value.toSet() }))
    )
    private val newWindows = mutableSetOf<ModuleWindow>()
    private val baseFlags = TreeNodeFlag.SpanFullWidth or TreeNodeFlag.OpenOnDoubleClick

    /**
     * Returns if this module has detached
     */
    private fun collapsibleModule(
        module: Module,
        source: ModuleWindow,
        sourceGroup: String
    ): ModuleWindow? {
        val nodeFlags = if (!module.isEnabled) baseFlags else (baseFlags or TreeNodeFlag.Selected)
        val label = "${module.name}-node"
        var moduleWindow: ModuleWindow? = null

        // We don't want imgui to handle open/closing at all, so we hack out the behaviour
        val doubleClicked = ImGui.io.mouseDoubleClicked[0]
        ImGui.io.mouseDoubleClicked[0] = false

        var clickedLeft = false
        var clickedRight = false

        fun updateClicked() {
            clickedLeft = isItemClicked(if (KamiSettings.swapModuleListButtons) MouseButton.Left else MouseButton.Right)
            clickedRight = isItemClicked(if (KamiSettings.swapModuleListButtons) MouseButton.Right else MouseButton.Left)
        }

        val open = treeNodeExV(label, nodeFlags, module.name)
        dragDropTarget {
            acceptDragDropPayload(KAMI_MODULE_PAYLOAD)?.let {
                val payload = Payloads.payload!!
                val dModules = payload.set

                // Start by removing the module(s) from the payload's source
                val newDropSourceGroups = mutableMapOf<String, Set<Module>>()
                for ((group, set) in payload.source.groups) {
                    val newSet = set.filter { !dModules.contains(it) }.toSet()
                    if (newSet.isNotEmpty()) {
                        newDropSourceGroups[group] = newSet
                    }
                }
                payload.source.groups = newDropSourceGroups

                // Add the modules to this window
                val newTargetGroups = source.groups.toMutableMap()
                val set = (newTargetGroups[sourceGroup] ?: error("No such group $sourceGroup")).toMutableSet()
                set.addAll(dModules)
                newTargetGroups[sourceGroup] = set
                source.groups = newTargetGroups

                Payloads.payload = null
            }
        }
        if (open) {
            updateClicked()
            ModuleSettings(module) {
                dragDropSource(DragDropFlag.SourceAllowNullID.i) {
                    setDragDropPayload(KAMI_MODULE_PAYLOAD, 0, 0) // no data
                    if (Payloads.needsPayload()) {
                        Payloads.payload = ModulePayload(mutableSetOf(module), source)
                    }
                    text("Merge")
                }
            }

            popupContextItem("$label-popup") {
                menuItem("Detach") {
                    moduleWindow = ModuleWindow(module.name, module = module)
                }
            }

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
        
        return moduleWindow
    }

    operator fun invoke() {
        windows.removeIf(ModuleWindow::draw)
        if (windows.addAll(newWindows)) {
            newWindows.clear()
        }
    }

    class ModuleWindow(private val title: String, val pos: Vec2? = null, var groups: Map<String, Set<Module>> = mapOf()) {

        constructor(title: String, pos: Vec2? = null, module: Module) : this(title, pos, mapOf(Pair("Group 1", setOf(module))))

        var closed = false

        fun draw(): Boolean {
            pos?.let {
                setNextWindowPos(pos, Cond.Appearing)
            }
            
            fun iterateModules(set: Iterable<Module>, group: String) {
                for (module in set) {
                    val moduleWindow = collapsibleModule(module, this, group)
                    moduleWindow?.let {
                        newWindows.add(moduleWindow)
                    }
                }
            }

            window(title){
                when {
                    groups.isEmpty() -> {
                        return true // close this window
                    }
                    groups.size == 1 -> {
                        val entry = groups.entries.stream().findAny().get()
                        val group = entry.value
                        if (group.isEmpty()) {
                            return true // close this window
                        }

                        iterateModules(group, entry.key)
                    }
                    else -> {
                        for ((group, set) in groups) {
                            if (set.isEmpty()) {
                                continue
                            }

                            if (treeNodeExV("cat-$group-node", TreeNodeFlag.SpanFullWidth.i, group)) {
                                iterateModules(set, group)
                                treePop()
                            }
                        }
                    }
                }
            }

            return closed
        }

    }


}