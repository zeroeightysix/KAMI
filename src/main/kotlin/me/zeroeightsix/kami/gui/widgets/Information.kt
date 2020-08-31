package me.zeroeightsix.kami.gui.widgets

import glm_.vec4.Vec4

object Information : TextPinnableWidget(
    "Information",
    mutableListOf(
        CompiledText(
            mutableListOf(
                CompiledText.VariablePart(varMap["client"]!!()),
                CompiledText.VariablePart(varMap["version"]!!(), extraspace = false)
            )
        ),
        CompiledText(
            mutableListOf(
                CompiledText.VariablePart(varMap["fps"]!!()),
                CompiledText.LiteralPart("fps").also {
                    it.colour = Vec4(1f, 1f, 1f, 0.64f)
                },
                CompiledText.VariablePart(varMap["tps"]!!()),
                CompiledText.LiteralPart("tps", extraspace = false).also {
                    it.colour = Vec4(1f, 1f, 1f, 0.64f)
                }
            )
        )
    ), position = Position.TOP_LEFT
)
