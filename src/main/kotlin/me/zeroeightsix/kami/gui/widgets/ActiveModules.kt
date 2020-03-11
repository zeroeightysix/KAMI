package me.zeroeightsix.kami.gui.widgets

import glm_.vec2.Vec2
import imgui.ImGui
import imgui.dsl.button
import imgui.dsl.child
import imgui.dsl.menuItem
import me.zeroeightsix.kami.backToString
import me.zeroeightsix.kami.feature.FeatureManager

object ActiveModules : TextPinnableWidget("Active modules", variableMap = extendStd(mapOf(
    "modules" to {
        object : CompiledText.StringVariable(_multiline = true, provider = {
            FeatureManager.modules.filter { it.isEnabled() && it.showInActiveModules.value }.joinToString("\n") { it.name.value }
        }) {
            var filter = ByteArray(128)
            override var editLabel: String = "(active modules)"

            override fun edit(variableMap: Map<String, () -> CompiledText.Variable>) {
                ImGui.separator()
                ImGui.text("Show the following modules in the list:")
                ImGui.inputText("Filter", filter)
                child("active-modules-show-list", Vec2(ImGui.windowContentRegionWidth, 60)) {
                    FeatureManager.modules.filter { !it.hidden && it.name.value.toLowerCase().contains(filter.backToString().toLowerCase()) }.forEach {
                        menuItem(it.name.value, selected = it.showInActiveModules.value) {
                            it.showInActiveModules.setValue(!it.showInActiveModules.value)
                        }
                    }
                }
                button("Check all") {
                    FeatureManager.modules.filter { !it.hidden }.forEach { it.showInActiveModules.value = true }
                }
                ImGui.sameLine()
                button("Uncheck all") {
                    FeatureManager.modules.filter { !it.hidden }.forEach { it.showInActiveModules.value = false }
                }
                ImGui.separator()
            }
        }
    }
)), text = mutableListOf(
    CompiledText(
        mutableListOf(
            CompiledText.VariablePart(getVariable("modules"), extraspace = false)
        )
    )
), position = Position.TOP_RIGHT) {
}