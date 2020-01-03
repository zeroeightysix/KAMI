package me.zeroeightsix.kami.gui.windows

import glm_.vec2.Vec2
import imgui.*
import imgui.ImGui.beginChild
import imgui.ImGui.currentWindow
import imgui.ImGui.endChild
import imgui.ImGui.isItemClicked
import imgui.ImGui.pushItemWidth
import imgui.ImGui.sameLine
import imgui.ImGui.setDragDropPayload
import imgui.ImGui.setNextWindowPos
import imgui.ImGui.text
import imgui.ImGui.treeNodeBehaviorIsOpen
import imgui.ImGui.treeNodeExV
import imgui.ImGui.treePop
import imgui.api.demoDebugInformations
import imgui.dsl.collapsingHeader
import imgui.dsl.dragDropSource
import imgui.dsl.dragDropTarget
import imgui.dsl.menuItem
import imgui.dsl.popupContextItem
import imgui.dsl.window
import imgui.internal.ItemStatusFlag
import imgui.internal.or
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.module.ModuleManager

object KamiModules {

    private val windows = mutableListOf<ModuleWindow>(
        MultiGroupWindow("All modules", ModuleManager.modules.groupBy {
            it.category.getName()
        }.mapValuesTo(mutableMapOf(), { entry -> entry.value.toSet().toMutableSet() }))
    )
    private val baseFlags = TreeNodeFlag.SpanFullWidth.i or TreeNodeFlag.OpenOnDoubleClick
    private val kamiModulePayload = "KAMI_MODULES"
    private lateinit var payload: ModulePayload
    private var needsPayload = true

    /**
     * Returns if this module has detached
     */
    private fun collapsibleModule(module: Module, windowIterator: MutableListIterator<ModuleWindow>, source: ModuleWindow): Boolean {
        val nodeFlags = if (!module.isEnabled) baseFlags else (baseFlags or TreeNodeFlag.Selected)
        val label = "${module.name}-node"
        var detached = false

        // We don't want imgui to handle open/closing at all, so we hack out the behaviour
        val doubleClicked = ImGui.io.mouseDoubleClicked[0]
        ImGui.io.mouseDoubleClicked[0] = false

        var clickedLeft = false
        var clickedRight = false

        fun updateClicked() {
            clickedLeft = isItemClicked(if (KamiSettings.swapModuleListButtons) MouseButton.Left else MouseButton.Right)
            clickedRight = isItemClicked(if (KamiSettings.swapModuleListButtons) MouseButton.Right else MouseButton.Left)
        }

        if (treeNodeExV(label, nodeFlags, module.name)) {
            updateClicked()
            ModuleSettings(module, source)

            popupContextItem("$label-popup") {
                menuItem("Detach") {
                    windowIterator.add(OneModuleWindow(module.name, module))
                    detached = true
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
        
        return detached
    }

    operator fun invoke() {
        // Use a iterator so we don't get any ConcurrentModificationExceptions when adding windows
        val iterator = windows.listIterator()
        while (iterator.hasNext()) {
            iterator.next().draw(iterator)
        }
    }
    
    abstract class ModuleWindow(val title: String, val pos: Vec2? = null) {
        
        var closed = false

        fun draw(windowIterator: MutableListIterator<ModuleWindow>) {
            if (closed) windowIterator.remove()
            else {
                pos?.let {
                    setNextWindowPos(pos, Cond.Appearing)
                }

                window(title) {
                    fill(windowIterator)
                }
            }
        }

        protected abstract fun fill(windowIterator: MutableListIterator<ModuleWindow>)
        abstract fun remove(modules: Set<Module>)

    }

    class OneModuleWindow(title: String, val module: Module, pos: Vec2? = null) : ModuleWindow(title, pos) {

        var moduleEnabled = module.isEnabled

        override fun fill(windowIterator: MutableListIterator<ModuleWindow>) {
            beginChild("$title-child")
            if (ImGui.checkbox("Enabled", ::moduleEnabled)) {
                module.isEnabled = moduleEnabled
            }
            ModuleSettings(module, this)
            endChild()

            dragDropTarget {
                ImGui.acceptDragDropPayload(kamiModulePayload)?.let {
                    // Upgrade from a OneModuleWindow to a MultiModuleWindow
                    closed = true
                    val pos = currentWindow.pos
                    val set = payload.list
                    set.add(module) // Add our one module to the set
                    val id = set.map { it.originalName }.joinToString("-")
                    val multi = MultiModuleWindow("${set.size} modules##$id", set, pos)
                    needsPayload = true
                    windowIterator.add(multi)
                }
            }
        }

        override fun remove(modules: Set<Module>) {
            closed = modules.contains(module)
        }

    }

    class MultiModuleWindow(title: String, val modules: MutableSet<Module>, pos: Vec2? = null) : ModuleWindow(title, pos) {

        override fun fill(windowIterator: MutableListIterator<ModuleWindow>) {
            beginChild("$title-child")
            val detachedModules = mutableSetOf<Module>()
            modules.forEach {
                if (collapsibleModule(it, windowIterator, this)) {
                    detachedModules.add(it)
                }
            }
            remove(detachedModules)
            endChild()
            dragDropTarget {
                ImGui.acceptDragDropPayload(kamiModulePayload)?.let {
                    val list = payload.list
                    payload.source.remove(list) // remove the module(s) from the payload source
                    modules.addAll(list) // add them all to our modules
                    needsPayload = true
                }
            }
        }

        override fun remove(modules: Set<Module>) {
            this.modules.removeAll(modules)
            closed = this.modules.isEmpty()
        }

    }
    
    class MultiGroupWindow(title: String, val groups: MutableMap<String, MutableSet<Module>>, pos: Vec2? = null) : ModuleWindow(title, pos) {

        override fun fill(windowIterator: MutableListIterator<ModuleWindow>) {
            val detachedModules = mutableSetOf<Module>()
            for ((group, list) in groups) {
                collapsingHeader(group) {
                    if (list.isEmpty()) {
                        text("Ain't nobody here but us chickens.")
                    } else {
                        list.forEach {
                            if (collapsibleModule(it, windowIterator, this)) {
                                detachedModules.add(it)
                            }
                        }
                    }
                }
            }
            remove(detachedModules)
        }

        override fun remove(modules: Set<Module>) {
            val iterator = groups.iterator()
            iterator.forEach {
                it.value.removeAll(modules)
                if (it.value.isEmpty())
                    iterator.remove()
            }
            closed = groups.isEmpty()
        }

    }

    object ModuleSettings {

        operator fun invoke(module: Module, source: ModuleWindow) {
            pushItemWidth(-10)
            text("These are the settings for ${module.name}.")
            sameLine()
            demoDebugInformations.helpMarker("Start dragging from this question mark to merge this module into another window.")
            dragDropSource(DragDropFlag.SourceAllowNullID.i) {
                setDragDropPayload(kamiModulePayload, "", 0)
                if (needsPayload) {
                    payload = ModulePayload(mutableSetOf(module), source)
                    needsPayload = false
                }
                text("Merge")
            }
        }

    }

    data class ModulePayload(var list: MutableSet<Module>, val source: ModuleWindow)

}