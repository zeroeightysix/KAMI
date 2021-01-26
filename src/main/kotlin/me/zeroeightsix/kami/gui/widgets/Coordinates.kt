package me.zeroeightsix.kami.gui.widgets

import me.zeroeightsix.kami.Colour
import me.zeroeightsix.kami.gui.text.CompiledText
import me.zeroeightsix.kami.gui.text.VarMap

object Coordinates : TextPinnableWidget(
    "Coordinates",
    text = mutableListOf(
        CompiledText(
            mutableListOf(
                CompiledText.VariablePart(VarMap["facing_axis"]!!()),
                CompiledText.VariablePart(VarMap["x"]!!()).also {
                    it.colour = Colour(0.64f, 1f, 1f, 1f)
                },
                CompiledText.VariablePart(VarMap["y"]!!()).also {
                    it.colour = Colour(0.64f, 1f, 1f, 1f)
                },
                CompiledText.VariablePart(VarMap["z"]!!()).also {
                    it.colour = Colour(0.64f, 1f, 1f, 1f)
                }
            )
        )
    ),
    position = Position.BOTTOM_LEFT
)
