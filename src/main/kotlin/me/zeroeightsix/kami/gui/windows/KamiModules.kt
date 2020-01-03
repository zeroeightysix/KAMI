package me.zeroeightsix.kami.gui.windows

import imgui.*
import imgui.ImGui.beginChild
import imgui.ImGui.currentWindow
import imgui.ImGui.endChild
import imgui.ImGui.isItemClicked
import imgui.ImGui.pushItemWidth
import imgui.ImGui.sameLine
import imgui.ImGui.setDragDropPayload
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
        MultiGroupWindow("All modules", ModuleManager.modules.groupByTo(mutableMapOf(), { it.category.getName() }))
    )
    private val baseFlags = TreeNodeFlag.SpanFullWidth.i or TreeNodeFlag.OpenOnDoubleClick
    private val kamiModulePayload = "KAMI_MODULES"
    private lateinit var payload: ModulePayload

    private fun collapsibleModule(module: Module, windowIterator: MutableListIterator<ModuleWindow>, source: ModuleWindow) {
        val nodeFlags = if (!module.isEnabled) baseFlags else (baseFlags or TreeNodeFlag.Selected)
        val label = "${module.name}-node"

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
    }

    operator fun invoke() {
        // Use a iterator so we don't get any ConcurrentModificationExceptions when adding windows
        val iterator = windows.listIterator()
        while (iterator.hasNext()) {
            iterator.next().draw(iterator)
        }
    }

    abstract class ModuleWindow(val title: String) {
        
        var closed = false

        fun draw(windowIterator: MutableListIterator<ModuleWindow>) {
            if (closed) windowIterator.remove()
            else
                window(title) {
                    fill(windowIterator)
                }
        }

        protected abstract fun fill(windowIterator: MutableListIterator<ModuleWindow>)
        abstract fun remove(modules: List<Module>)

    }

    class OneModuleWindow(title: String, val module: Module) : ModuleWindow(title) {

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

                    closed = true // removes this OneModuleWindow
                    val list = payload.list
                    list.add(0, module)
                    val id = list.map { it.originalName }.joinToString("-")
                    val multi = MultiModuleWindow("${list.size} modules##$id", list)
                    payload.list = mutableListOf() // make sure the next drag & drop doesn't affect this instance of payload
                    windowIterator.add(multi)
                }
            }
        }

        override fun remove(modules: List<Module>) {
            closed = modules.contains(module)
        }

    }

    class MultiModuleWindow(title: String, val modules: MutableList<Module>) : ModuleWindow(title) {

        override fun fill(windowIterator: MutableListIterator<ModuleWindow>) {
            beginChild("$title-child")
            dragDropTarget {
                ImGui.acceptDragDropPayload(kamiModulePayload)?.let {
                    val list = payload.list
                    payload.source.remove(list) // remove the module(s) from the payload source
                    modules.addAll(list) // add them all to our modules
                    list.clear() // clear the payload
                }
            }
            modules.forEach {
                collapsibleModule(it, windowIterator, this)
            }
            endChild()
        }

        override fun remove(modules: List<Module>) {
            this.modules.removeAll(modules)
            closed = modules.isEmpty()
        }

    }
    
    class MultiGroupWindow(title: String, val groups: MutableMap<String, MutableList<Module>>) : ModuleWindow(title) {

        override fun fill(windowIterator: MutableListIterator<ModuleWindow>) {
            for ((group, list) in groups) {
                collapsingHeader(group) {
                    if (list.isEmpty()) {
                        text("Ain't nobody here but us chickens.")
                    } else {
                        list.forEach {
                            collapsibleModule(it, windowIterator, this)
                        }
                    }
                }
            }
        }

        override fun remove(modules: List<Module>) {
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
                payload = ModulePayload(mutableListOf(module), source)
                text("Merge")
            }
        }

    }

    data class ModulePayload(var list: MutableList<Module>, val source: ModuleWindow)

}