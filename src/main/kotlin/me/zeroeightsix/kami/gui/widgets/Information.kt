package me.zeroeightsix.kami.gui.widgets

import me.zeroeightsix.kami.Colour
import me.zeroeightsix.kami.gui.text.CompiledText
import me.zeroeightsix.kami.gui.text.VarMap

object Information : TextPinnableWidget(
    "Information",
    mutableListOf(
        CompiledText(
            mutableListOf(
                CompiledText.VariablePart(VarMap["client"]!!()),
                CompiledText.VariablePart(VarMap["version"]!!(), extraSpace = false)
            )
        ),
        CompiledText(
            mutableListOf(
                CompiledText.VariablePart(VarMap["fps"]!!()),
                CompiledText.LiteralPart("fps").also {
                    it.colour = Colour(0.64f, 1f, 1f, 1f)
                },
                CompiledText.VariablePart(VarMap["tps"]!!()),
                CompiledText.LiteralPart("tps", extraSpace = false).also {
                    it.colour = Colour(0.64f, 1f, 1f, 1f)
                }
            )
        )
    ),
    position = Position.TOP_LEFT
)
