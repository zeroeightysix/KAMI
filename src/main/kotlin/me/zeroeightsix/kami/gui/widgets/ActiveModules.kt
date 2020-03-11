package me.zeroeightsix.kami.gui.widgets

import me.zeroeightsix.kami.feature.FeatureManager

object ActiveModules : TextPinnableWidget("Active modules", variableMap = extendStd(mapOf(
    "modules" to {
        object : CompiledText.StringVariable(_multiline = true, provider = {
            FeatureManager.modules.filter { it.isEnabled() }.joinToString("\n") { it.name.value }
        }) {
            override var editLabel: String = "(active modules)"
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