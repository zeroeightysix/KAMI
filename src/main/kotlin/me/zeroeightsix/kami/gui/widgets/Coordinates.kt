package me.zeroeightsix.kami.gui.widgets

import glm_.vec4.Vec4

object Coordinates : TextPinnableWidget(
    "Coordinates", text = mutableListOf(
        CompiledText(
            mutableListOf(
                CompiledText.VariablePart(varMap["facing_axis"]!!()),
                CompiledText.VariablePart(varMap["x"]!!()).also {
                    it.colour = Vec4(1f, 1f, 1f, 0.64f)
                },
                CompiledText.VariablePart(varMap["y"]!!()).also {
                    it.colour = Vec4(1f, 1f, 1f, 0.64f)
                },
                CompiledText.VariablePart(varMap["z"]!!()).also {
                    it.colour = Vec4(1f, 1f, 1f, 0.64f)
                }
            )
        )
    ), position = Position.BOTTOM_LEFT
)
