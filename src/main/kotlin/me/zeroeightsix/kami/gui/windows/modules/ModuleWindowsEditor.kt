package me.zeroeightsix.kami.gui.windows.modules

import imgui.ImGui
import imgui.ImGui.acceptDragDropPayload
import imgui.ImGui.checkbox
import imgui.ImGui.columns
import imgui.ImGui.nextColumn
import imgui.ImGui.sameLine
import imgui.ImGui.selectable
import imgui.ImGui.separator
import imgui.ImGui.setDragDropPayload
import imgui.ImGui.text
import imgui.ImGui.textDisabled
import imgui.MouseButton
import imgui.WindowFlag
import imgui.api.demoDebugInformations
import imgui.dsl.child
import imgui.dsl.dragDropSource
import imgui.dsl.dragDropTarget
import imgui.dsl.menu
import imgui.dsl.menuBar
import imgui.dsl.menuItem
import imgui.dsl.window
import me.zeroeightsix.kami.gui.windows.modules.Payloads.KAMI_MODULE_PAYLOAD

object ModuleWindowsEditor {

    var open = false
    private var rearrange = false

    operator fun invoke() {
        if (open) {
            window("Module windows editor", ::open, WindowFlag.MenuBar or WindowFlag.NoScrollbar or WindowFlag.NoScrollWithMouse.i) {
                menuBar {
                    menu("Edit") {
                        menuItem("Reset") {
                            Modules.reset()
                        }
                    }
                }

                checkbox("Rearrange modules", ::rearrange)
                sameLine()
                demoDebugInformations.helpMarker("While rearranging, you will not be able to move modules between groups or other windows.")

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
                            treeNodeAlways(group.key, block = {
                                group.value.forEachIndexed { n, module ->
                                    selectable(module.name)
                                    if (!rearrange) {
                                        dragDropSource() {
                                            setDragDropPayload(KAMI_MODULE_PAYLOAD, ModulePayload(mutableSetOf(module), window))
                                            text(module.name)
                                        }
                                    } else {
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
                            }, always = {
                                dragDropSource {
                                    setDragDropPayload(
                                        KAMI_MODULE_PAYLOAD,
                                        ModulePayload(group.value.toMutableSet(), window, group.key)
                                    )
                                    text("Group: ${group.key}")
                                }
                                dragDropTarget {
                                    acceptDragDropPayload(KAMI_MODULE_PAYLOAD)?.let {
                                        val payload = it.data!! as ModulePayload
                                        payload.moveTo(window, group.key)
                                    }
                                }
                            })
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
                        payload.moveTo(window, payload.groupName ?: "Group 1")
                    }
                }
                columns(1)
            }
        }
    }

    inline fun treeNodeAlways(label: String, block: () -> Unit, always: () -> Unit = {}) {
        if (ImGui.treeNode(label))
            try { always(); block() } finally {
                ImGui.treePop()
            }
        else
            always()
    }

}