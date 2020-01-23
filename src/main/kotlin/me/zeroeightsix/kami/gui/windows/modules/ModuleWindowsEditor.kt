package me.zeroeightsix.kami.gui.windows.modules

import imgui.ImGui
import imgui.ImGui.acceptDragDropPayload
import imgui.ImGui.columns
import imgui.ImGui.nextColumn
import imgui.ImGui.selectable
import imgui.ImGui.separator
import imgui.ImGui.setDragDropPayload
import imgui.ImGui.text
import imgui.ImGui.textDisabled
import imgui.MouseButton
import imgui.WindowFlag
import imgui.dsl.child
import imgui.dsl.dragDropSource
import imgui.dsl.dragDropTarget
import imgui.dsl.menu
import imgui.dsl.menuBar
import imgui.dsl.menuItem
import imgui.dsl.treeNode
import imgui.dsl.window
import me.zeroeightsix.kami.gui.windows.modules.Payloads.KAMI_MODULE_PAYLOAD

object ModuleWindowsEditor {

    var open = false

    operator fun invoke() {
        window("Module windows editor", ::open, WindowFlag.MenuBar or WindowFlag.NoScrollbar or WindowFlag.NoScrollWithMouse.i) {
            menuBar {
                menu("Edit") {
                    menuItem("Reset") {
                        Modules.reset()
                    }
                }
            }
            val windows = Modules.windows
            columns(windows.size + 1)
            for (window in windows) {
                text(window.title)
                nextColumn()
            }
            textDisabled("New window")
            nextColumn()
            separator()
            
            for (window in windows) {
                child("${window.title}-child") {
                    for (group in window.groups) {
                        if (group.value.isEmpty()) continue
                        // TODO: Drag & drop buggy.
                        // fix.
                        // not easy.
                        // i tried.
                        treeNode(group.key) {
                            group.value.forEachIndexed { n, module ->
                                selectable(module.name)
                                dragDropSource() {
                                    setDragDropPayload(KAMI_MODULE_PAYLOAD, ModulePayload(mutableSetOf(module), window))
                                    text(module.name)
                                }

                                val hovered = ImGui.isItemHovered()
                                if (ImGui.isItemActive && !hovered) {
                                    val nNext = n + if (ImGui.getMouseDragDelta(MouseButton.Left).y < 0f) -1 else 1
                                    if (nNext in group.value.indices) {
                                            group.value[n] = group.value[nNext]
                                            group.value[nNext] = module
                                            ImGui.resetMouseDragDelta()
                                    }
                                }
                            }
                        }
                        dragDropTarget {
                            acceptDragDropPayload(KAMI_MODULE_PAYLOAD)?.let {
                                val payload = it.data!! as ModulePayload
                                payload.moveTo(window, group.key)
                            }
                        }
                    }
                }
                nextColumn()
            }
            child("new-window-child") {}
            dragDropTarget {
                acceptDragDropPayload(KAMI_MODULE_PAYLOAD)?.let { 
                    val payload = it.data!! as ModulePayload
                    val window = Modules.ModuleWindow(payload.inventName())
                    windows.add(window)
                    payload.moveTo(window, "Group 1")
                }
            }
            columns(1)
        }
    }

}