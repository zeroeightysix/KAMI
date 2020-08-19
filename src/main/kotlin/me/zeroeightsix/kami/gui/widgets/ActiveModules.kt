package me.zeroeightsix.kami.gui.widgets

import glm_.vec2.Vec2
import imgui.ImGui
import imgui.dsl.button
import imgui.dsl.child
import imgui.dsl.menuItem
import me.zeroeightsix.kami.backToString
import me.zeroeightsix.kami.feature.FeatureManager

val modulesVariable = object : TextPinnableWidget.CompiledText.StringVariable("modules", true, {
    FeatureManager.modules.filter { it.enabled && it.showInActiveModules }.joinToString("\n") { it.name }
}) {
    var filter = ByteArray(128)
    override var editLabel: String = "(active modules)"

    override fun edit(variableMap: Map<String, () -> TextPinnableWidget.CompiledText.Variable>) {
        ImGui.separator()
        ImGui.text("Show the following modules in the list:")
        ImGui.inputText("Filter", filter)
        child("active-modules-show-list", Vec2(ImGui.windowContentRegionWidth, 60)) {
            FeatureManager.modules.filter {
                !it.hidden && it.name.toLowerCase().contains(filter.backToString().toLowerCase())
            }.forEach {
                menuItem(it.name, selected = it.showInActiveModules) {
                    it.showInActiveModules = !it.showInActiveModules
                }
            }
        }
        button("Check all") {
            FeatureManager.modules.filter { !it.hidden }.forEach { it.showInActiveModules = true }
        }
        ImGui.sameLine()
        button("Uncheck all") {
            FeatureManager.modules.filter { !it.hidden }.forEach { it.showInActiveModules = false }
        }
        ImGui.separator()
    }
}

object ActiveModules : TextPinnableWidget(
    "Active modules", text = mutableListOf(
        CompiledText(
            mutableListOf(
                CompiledText.VariablePart(modulesVariable, extraspace = false)
            )
        )
    ), position = Position.TOP_RIGHT
)
