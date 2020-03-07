package me.zeroeightsix.kami.gui.windows.modules

import imgui.*
import imgui.ImGui.acceptDragDropPayload
import imgui.ImGui.checkbox
import imgui.ImGui.columns
import imgui.ImGui.nextColumn
import imgui.ImGui.openPopup
import imgui.ImGui.sameLine
import imgui.ImGui.selectable
import imgui.ImGui.separator
import imgui.ImGui.setDragDropPayload
import imgui.ImGui.setNextItemWidth
import imgui.ImGui.text
import imgui.ImGui.textDisabled
import imgui.api.demoDebugInformations
import imgui.dsl.button
import imgui.dsl.child
import imgui.dsl.dragDropSource
import imgui.dsl.dragDropTarget
import imgui.dsl.menu
import imgui.dsl.menuBar
import imgui.dsl.menuItem
import imgui.dsl.popupContextItem
import imgui.dsl.popupModal
import imgui.dsl.window
import me.zeroeightsix.kami.gui.windows.modules.Payloads.KAMI_MODULE_PAYLOAD
import kotlin.collections.set
import kotlin.text.toByteArray

object ModuleWindowsEditor {

    var open = false
    private var rearrange = false
    private var modalPopup: (() -> Unit)? = null

    private fun ByteArray.backToString(): String {
        var str = ""
        for (c in this) {
            if (c == 0.toByte()) break
            str += c.toChar()
        }
        return str
    }

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
                    val buf = window.title.toByteArray(ByteArray(window.title.length + 2))
                    setNextItemWidth(-1f)
                    if (ImGui.inputText("###${window.hashCode()}-title-input", buf)) {
                        window.title = buf.backToString()
                    }
                    nextColumn()
                }
                textDisabled("New window")
                nextColumn()
                separator()

                for (window in windows) {
                    child("${window.hashCode()}-child") {
                        for (group in window.groups) {
                            if (group.value.isEmpty()) continue
                            // TODO: Drag & drop buggy.
                            // fix.
                            // not easy.
                            // i tried.
                            treeNodeAlways(group.key, block = {
                                group.value.forEachIndexed { n, module ->
                                    selectable(module.name.value)
                                    if (!rearrange) {
                                        dragDropSource {
                                            setDragDropPayload(KAMI_MODULE_PAYLOAD, ModulePayload(mutableSetOf(module), window))
                                            text(module.name.value)
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
                                popupContextItem("popup-${group.hashCode()}") {
                                    menuItem("Rename") {
                                        var name = group.key
                                        modalPopup = {
                                            fun rename() {
                                                val mutable = window.groups.toMutableMap()
                                                mutable.remove(group.key)
                                                mutable[name] = group.value
                                                window.groups = mutable
                                                ImGui.closeCurrentPopup()
                                                modalPopup = null
                                            }
                                            text("Rename to:")
                                            setNextItemWidth(-1f)
                                            if (ImGui.isWindowAppearing)
                                                ImGui.setKeyboardFocusHere()
                                            val buf = name.toByteArray(ByteArray(name.length + 2))
                                            if (ImGui.inputText("", name, flags = InputTextFlag.EnterReturnsTrue.i)) {
                                                name = buf.backToString()
                                                rename()
                                            }
                                            button("Rename") { rename() }
                                            sameLine()
                                            button("Cancel") {
                                                ImGui.closeCurrentPopup()
                                                modalPopup = null
                                            }
                                        }
                                    }
                                    menu("Sort") {
                                        menuItem("Alphabetically") {
                                            group.value.sortBy { it.name.value }
                                        }
                                        menuItem("Reverse alphabetically") {
                                            group.value.sortByDescending { it.name.value }
                                        }
                                    }
                                }
                            })
                        }
                    }
                    dragDropTarget {
                        acceptDragDropPayload(KAMI_MODULE_PAYLOAD)?.let {
                            val payload = it.data!! as ModulePayload
                            payload.moveTo(window, payload.groupName ?: "Group ${window.groups.size + 1}")
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
        if (modalPopup != null)
            openPopup("Rename group")
        popupModal("Rename group", null, WindowFlag.AlwaysAutoResize.i) {
            modalPopup?.let { it() }
        }
    }

    private inline fun treeNodeAlways(label: String, block: () -> Unit, always: () -> Unit = {}) {
        if (ImGui.treeNode(label))
            try { always(); block() } finally {
                ImGui.treePop()
            }
        else
            always()
    }

}