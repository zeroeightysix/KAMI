package me.zeroeightsix.kami.gui.windows.modules

import imgui.ImGui
import imgui.ImGui.acceptDragDropPayloadObject
import imgui.ImGui.columns
import imgui.ImGui.inputText
import imgui.ImGui.nextColumn
import imgui.ImGui.openPopup
import imgui.ImGui.sameLine
import imgui.ImGui.selectable
import imgui.ImGui.separator
import imgui.ImGui.setDragDropPayloadObject
import imgui.ImGui.setNextItemWidth
import imgui.ImGui.text
import imgui.ImGui.textDisabled
import imgui.flag.ImGuiInputTextFlags
import imgui.flag.ImGuiMouseButton
import imgui.flag.ImGuiWindowFlags
import kotlin.collections.set
import me.zeroeightsix.kami.gui.ImguiDSL.button
import me.zeroeightsix.kami.gui.ImguiDSL.checkbox
import me.zeroeightsix.kami.gui.ImguiDSL.child
import me.zeroeightsix.kami.gui.ImguiDSL.dragDropSource
import me.zeroeightsix.kami.gui.ImguiDSL.dragDropTarget
import me.zeroeightsix.kami.gui.ImguiDSL.helpMarker
import me.zeroeightsix.kami.gui.ImguiDSL.imgui
import me.zeroeightsix.kami.gui.ImguiDSL.menu
import me.zeroeightsix.kami.gui.ImguiDSL.menuBar
import me.zeroeightsix.kami.gui.ImguiDSL.menuItem
import me.zeroeightsix.kami.gui.ImguiDSL.popupContextItem
import me.zeroeightsix.kami.gui.ImguiDSL.popupModal
import me.zeroeightsix.kami.gui.ImguiDSL.window
import me.zeroeightsix.kami.gui.ImguiDSL.wrapImString
import me.zeroeightsix.kami.gui.windows.modules.Payloads.KAMI_MODULE_PAYLOAD

object ModuleWindowsEditor {

    var open = false
    private var rearrange = false
    private var modalPopup: (() -> Unit)? = null

    operator fun invoke() {
        if (open) {
            window(
                "Module windows editor",
                ::open,
                ImGuiWindowFlags.MenuBar or ImGuiWindowFlags.NoScrollbar or ImGuiWindowFlags.NoScrollWithMouse
            ) {
                menuBar {
                    menu("Edit") {
                        menuItem("Reset") {
                            Modules.reset()
                        }
                    }
                }

                checkbox("Rearrange modules", ::rearrange)
                sameLine()
                helpMarker("While rearranging, you will not be able to move modules between groups or other windows.")

                val windows = Modules.windows
                columns(windows.size + 1)
                for (window in windows) {
                    setNextItemWidth(-1f)
                    wrapImString(window::title) {
                        inputText("###${window.hashCode()}-title-input", it)
                    }
                    nextColumn()
                }
                textDisabled("New window")
                nextColumn()
                separator()

                for (window in windows) {
                    child("${window.hashCode()}-child") {
                        addWindowEditor(window)
                    }
                    dragDropTarget {
                        acceptDragDropPayloadObject(KAMI_MODULE_PAYLOAD)?.let {
                            val payload = it as ModulePayload
                            payload.moveTo(window, payload.groupName ?: "Group ${window.groups.size + 1}")
                        }
                    }
                    nextColumn()
                }
                child("new-window-child") {}
                dragDropTarget {
                    acceptDragDropPayloadObject(KAMI_MODULE_PAYLOAD)?.let {
                        val payload = it as ModulePayload
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
        popupModal("Rename group", null, ImGuiWindowFlags.AlwaysAutoResize) {
            modalPopup?.let { it() }
        }
    }

    private fun addWindowEditor(window: Modules.ModuleWindow) {
        for (group in window.groups) {
            if (group.value.isEmpty()) continue
            // TODO: Drag & drop buggy.
            // fix.
            // not easy.
            // i tried.
            treeNodeAlways(
                group.key,
                block = {
                    group.value.forEachIndexed { n, module ->
                        selectable(module.name)
                        if (!rearrange) {
                            dragDropSource {
                                setDragDropPayloadObject(
                                    KAMI_MODULE_PAYLOAD,
                                    ModulePayload(mutableSetOf(module), window)
                                )
                                text(module.name)
                            }
                        } else {
                            val hovered = ImGui.isItemHovered()
                            if (ImGui.isItemActive() && !hovered) {
                                val nNext =
                                    n + if (ImGui.getMouseDragDeltaY(ImGuiMouseButton.Left) < 0f) -1 else 1
                                if (nNext in group.value.indices) {
                                    group.value[n] = group.value[nNext]
                                    group.value[nNext] = module
                                    ImGui.resetMouseDragDelta()
                                }
                            }
                        }
                    }
                },
                always = {
                    dragDropSource {
                        setDragDropPayloadObject(
                            KAMI_MODULE_PAYLOAD,
                            ModulePayload(group.value.toMutableSet(), window, group.key)
                        )
                        text("Group: ${group.key}")
                    }
                    dragDropTarget {
                        acceptDragDropPayloadObject(KAMI_MODULE_PAYLOAD)?.let {
                            val payload = it as ModulePayload
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
                                if (ImGui.isWindowAppearing())
                                    ImGui.setKeyboardFocusHere()
                                val buf = name.imgui
                                if (inputText("", buf, ImGuiInputTextFlags.EnterReturnsTrue)) {
                                    name = buf.get()
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
                                group.value.sortBy { it.name }
                            }
                            menuItem("Reverse alphabetically") {
                                group.value.sortByDescending { it.name }
                            }
                        }
                    }
                }
            )
        }
    }

    private inline fun treeNodeAlways(label: String, block: () -> Unit, always: () -> Unit = {}) {
        if (ImGui.treeNode(label))
            try {
                always(); block()
            } finally {
                ImGui.treePop()
            } else
            always()
    }
}