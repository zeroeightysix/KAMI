package me.zeroeightsix.kami.gui.widgets

import me.zeroeightsix.kami.feature.FeatureManager

object ActiveModules : TextPinnableWidget("Active modules", variableMap = extendStd(mapOf(
    "modules" to {
        CompiledText.StringVariable() {
            FeatureManager.modules.filter { it.isEnabled() }.joinToString("\n") { it.name.value }
        }
    }
)), text = mutableListOf(
    CompiledText(
        mutableListOf(
            CompiledText.VariablePart(getVariable("modules"), extraspace = false)
        )
    )
)) {
}