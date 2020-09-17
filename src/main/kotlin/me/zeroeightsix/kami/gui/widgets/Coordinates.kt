package me.zeroeightsix.kami.gui.widgets

import glm_.vec4.Vec4
import me.zeroeightsix.kami.gui.text.CompiledText
import me.zeroeightsix.kami.gui.text.VarMap

object Coordinates : TextPinnableWidget(
    "Coordinates",
    text = mutableListOf(
        CompiledText(
            mutableListOf(
                CompiledText.VariablePart(VarMap["facing_axis"]!!()),
                CompiledText.VariablePart(VarMap["x"]!!()).also {
                    it.colour = Vec4(1f, 1f, 1f, 0.64f)
                },
                CompiledText.VariablePart(VarMap["y"]!!()).also {
                    it.colour = Vec4(1f, 1f, 1f, 0.64f)
                },
                CompiledText.VariablePart(VarMap["z"]!!()).also {
                    it.colour = Vec4(1f, 1f, 1f, 0.64f)
                }
            )
        )
    ),
    position = Position.BOTTOM_LEFT
)
